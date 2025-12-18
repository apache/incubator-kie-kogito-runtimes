/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.quarkus.serverless.workflow.opentelemetry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.kie.kogito.quarkus.serverless.workflow.opentelemetry.config.SonataFlowOtelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.EVENT_DESCRIPTION;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.RequestProperties;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SERVICE_NAME;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SERVICE_VERSION;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_ID;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_ID;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_STATE;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_VERSION;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_TRANSACTION_ID;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_WORKFLOW_STATE;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SpanNames;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.TrackerAttributes;

@ApplicationScoped
public class NodeSpanManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSpanManager.class);
    private final Tracer tracer;
    private final SonataFlowOtelConfig config;
    private final Map<String, ScopeManager> activeScopeManagers = new ConcurrentHashMap<>();
    private final Map<String, Span> lastActiveNodeSpan = new ConcurrentHashMap<>();

    @Inject
    public NodeSpanManager(Tracer tracer, SonataFlowOtelConfig config) {
        this.tracer = tracer;
        this.config = config;
    }

    record ScopeManager(Span span, Scope scope, String spanKey, Context previousContext) implements AutoCloseable {

        void endWithStatus(StatusCode statusCode, String description) {
            try {
                // Set status before cleanup
                if (description != null) {
                    span.setStatus(statusCode, description);
                } else {
                    span.setStatus(statusCode);
                }

                // End span first
                span.end();

                // Close scope (this should restore previousContext automatically)
                scope.close();

                // Context should be automatically restored by scope.close()
                // No validation needed as OpenTelemetry handles this internally

            } catch (Exception e) {
                LOGGER.error("Error ending span/scope for {}", spanKey, e);
                close();
            }
        }

        @Override
        public void close() {
            if (scope != null) {
                scope.close();
            }

            if (span != null) {
                span.end();
            }
        }
    }

    private Context determineParentContext(String processInstanceId, boolean isSubprocessNode) {
        if (isSubprocessNode) {
            LOGGER.debug("Using current context for subprocess node in process {}", processInstanceId);
            return Context.current();
        }

        Context rootContext = OtelContextHolder.getRootContext(processInstanceId);
        if (rootContext != null) {
            LOGGER.debug("Using stored root context for regular node in process {}", processInstanceId);
            return rootContext;
        }

        Context currentContext = Context.current();
        OtelContextHolder.setRootContext(processInstanceId, currentContext);
        LOGGER.debug("Captured and stored root context for first node in process {}", processInstanceId);
        return currentContext;
    }

    private boolean isSpanCreationEnabled() {
        return config.enabled() && config.spans().enabled();
    }

    private void registerScopeManager(ScopeManager scopeManager, String spanKey) {
        ScopeManager previousManager = activeScopeManagers.put(spanKey, scopeManager);
        if (previousManager != null) {
            previousManager.close();
            LOGGER.debug("Replaced previous span for {}", spanKey);
        }
        LOGGER.debug("Registered span for {}", spanKey);
    }

    public void addProcessEvent(Span span, String eventName, String description) {
        if (span != null) {
            if (description != null) {
                Attributes eventAttributes = Attributes.of(
                        EVENT_DESCRIPTION, description);
                span.addEvent(eventName, eventAttributes);
            } else {
                span.addEvent(eventName);
            }
            LOGGER.debug("Added event {} to span", eventName);
        } else {
            LOGGER.debug("Cannot add event {} - span is null", eventName);
        }
    }

    public void addProcessEvent(Span span, String eventName, Attributes attributes) {
        if (span != null) {
            span.addEvent(eventName, attributes);
            LOGGER.debug("Added process event {} to span", eventName);
        } else {
            LOGGER.debug("Cannot add event {} - span is null", eventName);
        }
    }

    public Span getLastActiveSpan(String processInstanceId) {
        return lastActiveNodeSpan.get(processInstanceId);
    }

    public Span getAnyActiveSpan(String processInstanceId) {
        return activeScopeManagers.entrySet().stream()
                .filter(forProcessInstance(processInstanceId))
                .map(entry -> entry.getValue().span())
                .findFirst()
                .orElse(null);
    }

    public void endRemainingSpans(String processInstanceId) {
        endRemainingSpansWithStatus(processInstanceId, StatusCode.OK, null);
    }

    public void endRemainingSpansWithError(String processInstanceId) {
        endRemainingSpansWithStatus(processInstanceId, StatusCode.ERROR, "Process failed with error");
    }

    private void endRemainingSpansWithStatus(String processInstanceId, StatusCode statusCode, String description) {
        String statePrefix = processInstanceId + ":state:";
        activeScopeManagers.entrySet().stream()
                .filter(forProcessInstance(processInstanceId))
                .forEach(entry -> {
                    ScopeManager manager = entry.getValue();
                    String spanKey = manager.spanKey();

                    String stateName = spanKey.substring(statePrefix.length());
                    String eventName = SonataFlowOtelAttributes.Events.STATE_COMPLETED;
                    String eventDescription = SonataFlowOtelAttributes.EventDescriptions.STATE_COMPLETED_PREFIX + stateName;

                    addProcessEvent(manager.span, eventName, eventDescription);
                    manager.endWithStatus(statusCode, description);
                    LOGGER.debug("Ended span for {} with status {}", spanKey, statusCode);
                });

        activeScopeManagers.entrySet().removeIf(forProcessInstance(processInstanceId));
        lastActiveNodeSpan.remove(processInstanceId);
        OtelContextHolder.clearRootContext(processInstanceId);
    }

    private Predicate<Map.Entry<String, ScopeManager>> forProcessInstance(String processInstanceId) {
        String prefix = processInstanceId + ":";
        return entry -> entry.getKey().startsWith(prefix);
    }

    public void setSpanError(Span span, Throwable exception, String description) {
        if (span != null) {
            span.setStatus(StatusCode.ERROR, description);
            if (exception != null) {
                span.recordException(exception);
            }
        }
    }

    public Span createStateSpan(String processInstanceId, String processId, String processVersion,
            String processState, String stateName) {
        return createStateSpan(processInstanceId, processId, processVersion, processState, stateName, false);
    }

    public Span createStateSpan(String processInstanceId, String processId, String processVersion,
            String processState, String stateName, boolean isSubprocessNode) {
        if (!isSpanCreationEnabled()) {
            LOGGER.debug("Span creation disabled");
            return null;
        }

        ScopeManager scopeManager = null;
        String spanKey = null;
        try {
            Context previousContext = Context.current();
            Context parentContext = determineParentContext(processInstanceId, isSubprocessNode);

            spanKey = buildStateSpanKey(processInstanceId, stateName);
            Span span = buildStateSpan(processInstanceId, processId, processVersion, processState, stateName, parentContext);
            Scope scope = span.makeCurrent();

            scopeManager = new ScopeManager(span, scope, spanKey, previousContext);
            registerScopeManager(scopeManager, spanKey);

            lastActiveNodeSpan.put(processInstanceId, span);
            return span;
        } catch (Exception e) {
            if (scopeManager != null) {
                scopeManager.close();
                if (spanKey != null) {
                    activeScopeManagers.remove(spanKey);
                    lastActiveNodeSpan.remove(processInstanceId);
                }
            }
            LOGGER.error("Failed to create state span for {}:{}", processInstanceId, stateName, e);
            return null;
        }
    }

    private String buildStateSpanKey(String processInstanceId, String stateName) {
        return processInstanceId + ":state:" + stateName;
    }

    private Span buildStateSpan(String processInstanceId, String processId, String processVersion,
            String processState, String stateName, Context parentContext) {
        String spanName = SpanNames.createProcessSpanName(processId);

        return tracer.spanBuilder(spanName)
                .setParent(parentContext)
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_ID, processInstanceId)
                .setAttribute(SONATAFLOW_PROCESS_ID, processId)
                .setAttribute(SONATAFLOW_PROCESS_VERSION, processVersion)
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_STATE, processState)
                .setAttribute(SERVICE_NAME, config.serviceName())
                .setAttribute(SERVICE_VERSION, config.serviceVersion())
                .setAttribute(SONATAFLOW_WORKFLOW_STATE, stateName)
                .startSpan();
    }

    public Span createStateSpanWithContext(String processInstanceId, String processId, String processVersion,
            String processState, String stateName, Map<String, String> headerContext, boolean isSubprocessNode) {
        Span span = createStateSpan(processInstanceId, processId, processVersion, processState, stateName, isSubprocessNode);

        if (span != null) {
            String transactionId = null;

            if (headerContext != null && !headerContext.isEmpty()) {
                transactionId = headerContext.get(RequestProperties.TRANSACTION_ID);

                for (Map.Entry<String, String> entry : headerContext.entrySet()) {
                    if (entry.getKey().startsWith(RequestProperties.TRACKER_PREFIX)) {
                        String attributeKey = TrackerAttributes.createTrackerAttributeKey(entry.getKey());
                        span.setAttribute(attributeKey, entry.getValue());
                    }
                }
            }

            if (transactionId == null) {
                transactionId = processInstanceId;
            }

            span.setAttribute(SONATAFLOW_TRANSACTION_ID, transactionId);
        }

        return span;
    }

    public Span getActiveStateSpan(String processInstanceId, String stateName) {
        String spanKey = buildStateSpanKey(processInstanceId, stateName);
        ScopeManager manager = activeScopeManagers.get(spanKey);
        return manager != null ? manager.span() : null;
    }

    public void endStateSpan(String processInstanceId, String stateName) {
        String spanKey = buildStateSpanKey(processInstanceId, stateName);
        ScopeManager manager = activeScopeManagers.remove(spanKey);
        if (manager != null) {
            manager.endWithStatus(StatusCode.OK, null);
            LOGGER.debug("Ended state span for {}", spanKey);
        }
    }

    @VisibleForTesting
    int getActiveScopeCount() {
        return activeScopeManagers.size();
    }

    @VisibleForTesting
    int getActiveSpanCount() {
        return lastActiveNodeSpan.size();
    }

    @PreDestroy
    public void cleanup() {
        int spanCount = activeScopeManagers.size();
        if (spanCount > 0) {
            LOGGER.debug("Cleaning up {} active spans during shutdown", spanCount);
            activeScopeManagers.values().forEach(manager -> {
                try {
                    manager.close();
                } catch (Exception e) {
                    LOGGER.warn("Error ending span for {}", manager.spanKey(), e);
                }
            });
        }

        activeScopeManagers.clear();
        lastActiveNodeSpan.clear();
    }
}

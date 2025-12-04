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
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_NODE;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_STATE;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_VERSION;
import static org.kie.kogito.quarkus.serverless.workflow.opentelemetry.SonataFlowOtelAttributes.SONATAFLOW_TRANSACTION_ID;
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

                // Validate context was properly restored
                Context currentContext = Context.current();
                if (!currentContext.equals(previousContext)) {
                    LOGGER.debug("Context changed after scope close for {} - may be expected in complex scenarios", spanKey);
                }

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

    public Span createNodeSpan(String processInstanceId, String processId, String processVersion, String processState, String nodeId) {
        if (!isSpanCreationEnabled()) {
            LOGGER.debug("Span creation disabled");
            return null;
        }

        ScopeManager scopeManager = null;
        try {
            Context previousContext = Context.current(); // Capture before creating span

            String spanKey = buildSpanKey(processInstanceId, nodeId);
            Span span = buildSpan(processInstanceId, processId, processVersion, processState, nodeId);
            Scope scope = span.makeCurrent();

            scopeManager = new ScopeManager(span, scope, spanKey, previousContext);
            registerScopeManager(scopeManager, spanKey);

            lastActiveNodeSpan.put(processInstanceId, span);
            return span;
        } catch (Exception e) {
            if (scopeManager != null) {
                scopeManager.close();
                activeScopeManagers.remove(scopeManager.spanKey());
            }
            LOGGER.error("Failed to create node span for {}:{}", processInstanceId, nodeId, e);
            return null;
        }
    }

    private boolean isSpanCreationEnabled() {
        return config.enabled() && config.spans().enabled();
    }

    private String buildSpanKey(String processInstanceId, String nodeId) {
        return processInstanceId + ":" + nodeId;
    }

    private Span buildSpan(String processInstanceId, String processId, String processVersion, String processState, String nodeId) {
        String spanName = SpanNames.createProcessSpanName(processId);

        return tracer.spanBuilder(spanName)
                .setParent(Context.current())
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_ID, processInstanceId)
                .setAttribute(SONATAFLOW_PROCESS_ID, processId)
                .setAttribute(SONATAFLOW_PROCESS_VERSION, processVersion)
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_STATE, processState)
                .setAttribute(SERVICE_NAME, config.serviceName())
                .setAttribute(SERVICE_VERSION, config.serviceVersion())
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_NODE, nodeId)
                .startSpan();
    }

    private void registerScopeManager(ScopeManager scopeManager, String spanKey) {
        ScopeManager previousManager = activeScopeManagers.put(spanKey, scopeManager);
        if (previousManager != null) {
            previousManager.close();
            LOGGER.debug("Replaced previous span for {}", spanKey);
        }
        LOGGER.debug("Registered span for {}", spanKey);
    }

    public Span getActiveNodeSpan(String processInstanceId, String nodeId) {
        String spanKey = buildSpanKey(processInstanceId, nodeId);
        ScopeManager manager = activeScopeManagers.get(spanKey);
        return manager != null ? manager.span() : null;
    }

    public void completeNodeSpan(Object event) {
        if (!(event instanceof org.kie.api.event.process.ProcessNodeLeftEvent nodeLeftEvent)) {
            return;
        }

        String processInstanceId = nodeLeftEvent.getProcessInstance().getId();
        String nodeId = nodeLeftEvent.getNodeInstance().getNodeName();
        String spanKey = buildSpanKey(processInstanceId, nodeId);

        ScopeManager manager = activeScopeManagers.remove(spanKey);
        if (manager != null) {
            manager.endWithStatus(StatusCode.OK, null);
            LOGGER.debug("Completed span for {}", spanKey);
        }
    }

    public void addProcessEvent(Span span, String eventName, String description) {
        if (span != null) {
            Attributes eventAttributes = Attributes.of(
                    EVENT_DESCRIPTION, description);
            span.addEvent(eventName, eventAttributes);
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
        activeScopeManagers.entrySet().stream()
                .filter(forProcessInstance(processInstanceId))
                .forEach(entry -> {
                    ScopeManager manager = entry.getValue();
                    manager.endWithStatus(statusCode, description);
                    LOGGER.debug("Ended span for {} with status {}", manager.spanKey(), statusCode);
                });

        activeScopeManagers.entrySet().removeIf(forProcessInstance(processInstanceId));
        lastActiveNodeSpan.remove(processInstanceId);
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

    public Span createNodeSpanWithContext(String processInstanceId, String processId, String processVersion,
            String processState, String nodeId, Map<String, String> headerContext) {
        Span span = createNodeSpan(processInstanceId, processId, processVersion, processState, nodeId);

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

    @VisibleForTesting
    int getActiveScopeCount() {
        return activeScopeManagers.size();
    }

    @VisibleForTesting
    int getActiveSpanCount() {
        return lastActiveNodeSpan.size();
    }

    @VisibleForTesting
    boolean hasActiveScope(String processInstanceId, String nodeId) {
        String spanKey = buildSpanKey(processInstanceId, nodeId);
        return activeScopeManagers.containsKey(spanKey);
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

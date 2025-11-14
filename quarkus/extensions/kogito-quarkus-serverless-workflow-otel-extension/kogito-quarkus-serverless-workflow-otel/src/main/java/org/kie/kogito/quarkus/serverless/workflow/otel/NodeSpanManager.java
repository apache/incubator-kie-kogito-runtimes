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
package org.kie.kogito.quarkus.serverless.workflow.otel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.kogito.quarkus.serverless.workflow.otel.config.SonataFlowOtelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.EVENT_DESCRIPTION;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.RequestProperties;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SERVICE_NAME;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SERVICE_VERSION;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_ID;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_ID;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_NODE;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_INSTANCE_STATE;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_PROCESS_VERSION;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SONATAFLOW_TRANSACTION_ID;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.SpanNames;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.TrackerAttributes;

@ApplicationScoped
public class NodeSpanManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeSpanManager.class);
    private final Tracer tracer;
    private final SonataFlowOtelConfig config;
    private final Map<String, Span> activeNodeSpans = new ConcurrentHashMap<>();
    private final Map<String, Scope> activeNodeScopes = new ConcurrentHashMap<>();
    private final Map<String, Span> lastActiveNodeSpan = new ConcurrentHashMap<>();

    @Inject
    public NodeSpanManager(Tracer tracer, SonataFlowOtelConfig config) {
        this.tracer = tracer;
        this.config = config;
    }

    public Span createNodeSpan(String processInstanceId, String processId, String processVersion, String processState, String nodeId) {
        if (!config.enabled() || !config.spans().enabled()) {
            return null;
        }

        String spanName = SpanNames.createProcessSpanName(processId);

        Span span = tracer.spanBuilder(spanName)
                .setParent(Context.current()) // Use current context for trace propagation
                .setSpanKind(SpanKind.INTERNAL)
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_ID, processInstanceId)
                .setAttribute(SONATAFLOW_PROCESS_ID, processId)
                .setAttribute(SONATAFLOW_PROCESS_VERSION, processVersion)
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_STATE, processState)
                .setAttribute(SERVICE_NAME, config.serviceName())
                .setAttribute(SERVICE_VERSION, config.serviceVersion())
                .setAttribute(SONATAFLOW_PROCESS_INSTANCE_NODE, nodeId)
                .startSpan();

        String spanKey = processInstanceId + ":" + nodeId;
        Scope scope = span.makeCurrent();
        activeNodeSpans.put(spanKey, span);
        activeNodeScopes.put(spanKey, scope);
        lastActiveNodeSpan.put(processInstanceId, span);
        LOGGER.debug("Created and activated node span for {}", spanKey);
        return span;
    }

    public Span getActiveNodeSpan(String processInstanceId, String nodeId) {
        String spanKey = processInstanceId + ":" + nodeId;
        return activeNodeSpans.get(spanKey);
    }

    public void completeNodeSpan(Object event) {
        if (event instanceof org.kie.api.event.process.ProcessNodeLeftEvent) {
            org.kie.api.event.process.ProcessNodeLeftEvent nodeLeftEvent =
                    (org.kie.api.event.process.ProcessNodeLeftEvent) event;

            String processInstanceId = nodeLeftEvent.getProcessInstance().getId();
            String nodeId = ((org.kie.kogito.internal.process.runtime.KogitoNodeInstance) nodeLeftEvent.getNodeInstance()).getNodeName();
            String spanKey = processInstanceId + ":" + nodeId;

            Span span = activeNodeSpans.remove(spanKey);
            Scope scope = activeNodeScopes.remove(spanKey);
            if (span != null) {
                if (scope != null) {
                    scope.close();
                }
                span.setStatus(StatusCode.OK);
                span.end();
                LOGGER.debug("Completed and deactivated node span for {}", spanKey);
            }
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
        return activeNodeSpans.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(processInstanceId + ":"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public void endRemainingSpans(String processInstanceId) {
        activeNodeSpans.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(processInstanceId + ":"))
                .forEach(entry -> {
                    String spanKey = entry.getKey();
                    Span span = entry.getValue();
                    Scope scope = activeNodeScopes.get(spanKey);

                    if (scope != null) {
                        scope.close();
                        activeNodeScopes.remove(spanKey);
                    }

                    span.setStatus(StatusCode.OK);
                    span.end();
                    LOGGER.debug("Ended remaining span for {}", spanKey);
                });

        activeNodeSpans.entrySet().removeIf(entry -> entry.getKey().startsWith(processInstanceId + ":"));
        lastActiveNodeSpan.remove(processInstanceId);
    }

    public void endRemainingSpansWithError(String processInstanceId) {
        activeNodeSpans.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(processInstanceId + ":"))
                .forEach(entry -> {
                    String spanKey = entry.getKey();
                    Span span = entry.getValue();
                    Scope scope = activeNodeScopes.get(spanKey);

                    if (scope != null) {
                        scope.close();
                        activeNodeScopes.remove(spanKey);
                    }

                    span.setStatus(StatusCode.ERROR, "Process failed with error");
                    span.end();
                    LOGGER.debug("Ended remaining span with error for {}", spanKey);
                });

        activeNodeSpans.entrySet().removeIf(entry -> entry.getKey().startsWith(processInstanceId + ":"));
        lastActiveNodeSpan.remove(processInstanceId);
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
}

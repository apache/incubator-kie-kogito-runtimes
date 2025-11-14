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

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoNodeInstance;
import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.quarkus.serverless.workflow.otel.config.SonataFlowOtelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;

import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.EventDescriptions;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.Events;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.NodePatterns;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ProcessStates;

/**
 * OpenTelemetry event listener for Kogito Serverless Workflow processes.
 * Handles node-level and process-level span creation and event emission.
 *
 * <p>
 * <b>Important Lifecycle Note:</b> The jBPM/serverless workflow engine does NOT invoke
 * afterProcessCompleted() for processes that terminate in ERROR state. Therefore, error
 * detection and event emission occurs in afterNodeLeft() when STATE_ERROR is detected.
 * This ensures that error events are properly captured for failed workflow executions.
 *
 * <p>
 * The implementation uses atomic duplicate detection to prevent multiple error events
 * for the same process failure when multiple nodes might trigger ERROR state detection.
 * Memory management is handled by immediate cleanup in the error handling path since
 * the normal cleanup in afterProcessCompleted() is never reached for failed processes.
 */
public class NodeOtelEventListener extends DefaultKogitoProcessEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeOtelEventListener.class);

    private final NodeSpanManager spanManager;
    private final SonataFlowOtelConfig config;
    private final ProcessEventHandler processEventHandler;
    private final HeaderContextExtractor headerExtractor;
    private final Set<String> processedErrors = ConcurrentHashMap.newKeySet();

    public NodeOtelEventListener(NodeSpanManager spanManager, SonataFlowOtelConfig config, HeaderContextExtractor headerExtractor) {
        this.spanManager = spanManager;
        this.config = config;
        this.headerExtractor = headerExtractor;
        this.processEventHandler = new ProcessEventHandler(spanManager, config);
        LOGGER.debug("NodeOtelEventListener initialized");
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        LOGGER.debug("Process started event received - start event will be added in beforeNodeTriggered");
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        try {
            KogitoNodeInstance nodeInstance = (KogitoNodeInstance) event.getNodeInstance();
            KogitoProcessInstance processInstance = (KogitoProcessInstance) event.getProcessInstance();

            String processInstanceId = processInstance.getId();
            String processId = processInstance.getProcessId();
            String processVersion = processInstance.getProcessVersion();
            String processState = getProcessState(processInstance.getState());
            String nodeId = nodeInstance.getNodeName();

            if (isInternalNode(nodeId)) {
                LOGGER.trace("Skipping internal node: {}", nodeId);
                return;
            }

            LOGGER.debug("Node triggered: {} for process instance {} (process: {})", nodeId, processInstanceId, processId);
            Map<String, String> extractedContext = OtelContextHolder.getExtractedContext();

            if (extractedContext.isEmpty()) {
                try {
                    Map<String, java.util.List<String>> processHeaders = processInstance.getHeaders();
                    if (processHeaders != null && !processHeaders.isEmpty()) {
                        Map<String, String> headerContext = headerExtractor.extractFromProcessHeaders(processHeaders);
                        if (!headerContext.isEmpty()) {
                            populateMDCFromContext(headerContext);
                            extractedContext = headerContext;
                            LOGGER.debug("Re-established context from process headers for process instance {}: {}", processInstanceId, headerContext.keySet());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.debug("Failed to extract context from process headers: {}", e.getMessage());
                }
            }

            Span span = spanManager.createNodeSpanWithContext(processInstanceId, processId, processVersion, processState, nodeId, extractedContext);

            if (span != null) {
                spanManager.addProcessEvent(span, Events.NODE_STARTED, EventDescriptions.NODE_STARTED_PREFIX + nodeId);
                processEventHandler.handleProcessStartEvent(span, processInstanceId);
            }
        } catch (Exception e) {
            LOGGER.error("Error in beforeNodeTriggered: {}", e.getMessage(), e);
        }
    }

    private boolean isInternalNode(String nodeId) {
        return nodeId != null && (nodeId.equals(NodePatterns.START) || nodeId.equals(NodePatterns.END) ||
                nodeId.startsWith(NodePatterns.JOIN_PREFIX) || nodeId.startsWith(NodePatterns.EMBEDDED_PREFIX) ||
                nodeId.equals(NodePatterns.SCRIPT) || nodeId.endsWith(NodePatterns.FUNCTION_SUFFIX));
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        try {
            KogitoProcessInstance processInstance = (KogitoProcessInstance) event.getProcessInstance();
            String processInstanceId = processInstance.getId();
            String outcome = getProcessState(processInstance.getState());

            long startTime = processInstance.getStartDate().getTime();
            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;

            LOGGER.debug("Process completed: processInstanceId={}, duration={}ms, outcome={}",
                    processInstanceId, durationMs, outcome);

            processedErrors.remove(processInstanceId);

            handleProcessLevelCompletionEvents(processInstance, processInstanceId, durationMs, outcome);

        } catch (Exception e) {
            LOGGER.error("Error in afterProcessCompleted: {}", e.getMessage(), e);
        }
    }

    private void handleProcessLevelCompletionEvents(KogitoProcessInstance processInstance,
            String processInstanceId,
            long durationMs,
            String outcome) {

        if (config.events().enabled() && config.events().processEvents().enabled()) {

            Span targetSpan = spanManager.getAnyActiveSpan(processInstanceId);

            if (targetSpan != null) {
                if (processInstance.getState() == ProcessInstance.STATE_ERROR) {
                    processEventHandler.handleProcessErrorEvent(targetSpan, processInstanceId, processInstance);
                }

                if (config.events().processEvents().includeComplete()) {
                    processEventHandler.addProcessCompleteEvent(targetSpan, processInstanceId, durationMs, outcome);
                    LOGGER.debug("Added process.instance.complete event for process instance {}", processInstanceId);
                }

            } else if (processInstance.getState() == ProcessInstance.STATE_ERROR) {
                processEventHandler.handleProcessErrorWithoutSpans(processInstance, durationMs);
            }
        }

        OtelContextHolder.clearProcessContexts(processInstanceId);
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        try {
            KogitoNodeInstance nodeInstance = (KogitoNodeInstance) event.getNodeInstance();
            KogitoProcessInstance processInstance = (KogitoProcessInstance) event.getProcessInstance();

            String processInstanceId = processInstance.getId();
            String nodeId = nodeInstance.getNodeName();

            if (isInternalNode(nodeId)) {
                LOGGER.trace("Skipping internal node on afterNodeLeft: {}", nodeId);
                return;
            }

            LOGGER.debug("Node left: {} for process instance {}", nodeId, processInstanceId);

            Span span = spanManager.getActiveNodeSpan(processInstanceId, nodeId);
            if (span != null) {
                spanManager.addProcessEvent(span, Events.NODE_COMPLETED, EventDescriptions.NODE_COMPLETED_PREFIX + nodeId);
            }

            spanManager.completeNodeSpan(event);

            if (processInstance.getState() == ProcessInstance.STATE_ERROR) {
                handleProcessErrorAfterNodeLeft(processInstance, processInstanceId);
            }
        } catch (Exception e) {
            LOGGER.error("Error in afterNodeLeft: {}", e.getMessage(), e);
        }
    }

    private String getProcessState(int state) {
        switch (state) {
            case ProcessInstance.STATE_PENDING:
                return ProcessStates.PENDING;
            case ProcessInstance.STATE_ACTIVE:
                return ProcessStates.ACTIVE;
            case ProcessInstance.STATE_COMPLETED:
                return ProcessStates.COMPLETED;
            case ProcessInstance.STATE_ABORTED:
                return ProcessStates.ABORTED;
            case ProcessInstance.STATE_SUSPENDED:
                return ProcessStates.SUSPENDED;
            case ProcessInstance.STATE_ERROR:
                return ProcessStates.ERROR;
            default:
                return ProcessStates.UNKNOWN;
        }
    }

    private void populateMDCFromContext(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : context.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if ("transaction.id".equals(key)) {
                OtelContextHolder.setTransactionId(value);
            } else if (key.startsWith("tracker.")) {
                OtelContextHolder.setTrackerAttribute(key, value);
            }
        }
    }

    /**
     * Handles process error events when ERROR state is detected in afterNodeLeft.
     * This is necessary because afterProcessCompleted() is never called for failed processes.
     * Uses atomic duplicate detection to prevent multiple error events for the same failure.
     *
     * @param processInstance the failed process instance
     * @param processInstanceId the process instance ID
     */
    private void handleProcessErrorAfterNodeLeft(KogitoProcessInstance processInstance, String processInstanceId) {
        if (processInstance == null || processInstanceId == null) {
            LOGGER.error("Cannot handle process error: null processInstance or processInstanceId");
            return;
        }

        // Atomic check-and-set operation to prevent race conditions
        if (!processedErrors.add(processInstanceId)) {
            LOGGER.debug("Error already handled for process instance {}", processInstanceId);
            return;
        }

        try {
            long durationMs = calculateProcessDuration(processInstance);

            if (config.events().enabled() && config.events().processEvents().enabled()) {
                Span targetSpan = spanManager.getAnyActiveSpan(processInstanceId);

                if (targetSpan != null) {
                    // Add error event to existing span
                    processEventHandler.handleProcessErrorEvent(targetSpan, processInstanceId, processInstance);

                    // Also add completion event with ERROR outcome
                    if (config.events().processEvents().includeComplete()) {
                        processEventHandler.addProcessCompleteEvent(targetSpan, processInstanceId, durationMs, ProcessStates.ERROR);
                    }
                } else {
                    // Create dedicated error span
                    processEventHandler.handleProcessErrorWithoutSpans(processInstance, durationMs);
                }
            }

            // Clean up process contexts
            OtelContextHolder.clearProcessContexts(processInstanceId);

        } catch (Exception e) {
            LOGGER.error("Error handling process error for {}: {}", processInstanceId, e.getMessage(), e);
        } finally {
            // Always cleanup tracking to prevent memory leak
            // This is critical because afterProcessCompleted() is never called for ERROR state
            processedErrors.remove(processInstanceId);
        }
    }

    /**
     * Calculates the duration of a process instance from start to current time.
     *
     * @param processInstance the process instance
     * @return duration in milliseconds, or 0 if start date is unavailable
     */
    private long calculateProcessDuration(KogitoProcessInstance processInstance) {
        try {
            Date startDate = processInstance.getStartDate();
            if (startDate != null) {
                return System.currentTimeMillis() - startDate.getTime();
            }
        } catch (Exception e) {
            LOGGER.debug("Could not calculate process duration: {}", e.getMessage());
        }
        return 0L;
    }
}

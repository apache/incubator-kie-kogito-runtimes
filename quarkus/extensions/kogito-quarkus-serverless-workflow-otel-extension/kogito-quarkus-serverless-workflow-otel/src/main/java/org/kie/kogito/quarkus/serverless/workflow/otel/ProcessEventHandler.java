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

import org.kie.kogito.internal.process.runtime.KogitoProcessInstance;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.quarkus.serverless.workflow.otel.config.SonataFlowOtelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;

import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.DURATION_MS;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ERROR_MESSAGE;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ERROR_TYPE;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ErrorConstants;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.NodeNames;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.OUTCOME;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.PROCESS_INSTANCE_ID;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ProcessContextStorage;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.ProcessStates;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.REFERENCE_ID;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.TRIGGER;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.TriggerTypes;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.VariableNames;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.Events.PROCESS_INSTANCE_COMPLETE;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.Events.PROCESS_INSTANCE_ERROR;
import static org.kie.kogito.quarkus.serverless.workflow.otel.SonataFlowOtelAttributes.Events.PROCESS_INSTANCE_START;

/**
 * Handles process lifecycle events for OpenTelemetry integration.
 * This class manages the creation and enrichment of process-level events such as
 * process start, completion, and error events on OpenTelemetry spans.
 */
public class ProcessEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEventHandler.class);

    private final NodeSpanManager spanManager;
    private final SonataFlowOtelConfig config;

    public ProcessEventHandler(NodeSpanManager spanManager, SonataFlowOtelConfig config) {
        this.spanManager = spanManager;
        this.config = config;
    }

    /**
     * Handles the process start event by adding it to the span.
     * Ensures the event is only added once per process instance.
     *
     * @param span the target span
     * @param processInstanceId the process instance ID
     */
    public void handleProcessStartEvent(Span span, String processInstanceId) {
        if (config.events().enabled() && config.events().processEvents().enabled() &&
                config.events().processEvents().includeStart()) {
            String startTransactionId = OtelContextHolder.getProcessStartContext(processInstanceId);

            if (startTransactionId == null) {
                String transactionId = OtelContextHolder.getTransactionId();
                if (transactionId == null) {
                    transactionId = processInstanceId;
                }

                Attributes startEventAttributes = buildProcessStartEventAttributes(processInstanceId, transactionId);
                spanManager.addProcessEvent(span, PROCESS_INSTANCE_START, startEventAttributes);

                OtelContextHolder.setProcessStartContext(processInstanceId, ProcessContextStorage.ADDED);
                LOGGER.debug("Added process.instance.start event for process instance {}", processInstanceId);
            }
        }
    }

    /**
     * Handles process errors when no active node spans exist.
     * Creates a dedicated error span for the failed process.
     *
     * @param processInstance the failed process instance
     * @param durationMs the process duration in milliseconds
     */
    public void handleProcessErrorWithoutSpans(KogitoProcessInstance processInstance, long durationMs) {
        String processInstanceId = processInstance.getId();

        Span existingSpan = spanManager.getAnyActiveSpan(processInstanceId);
        if (existingSpan != null) {
            LOGGER.debug("Process error will be handled via existing spans for {}", processInstanceId);
            return;
        }

        if (config.events().enabled() && config.events().processEvents().enabled()) {
            try {
                Map<String, String> extractedContext = OtelContextHolder.getExtractedContext();

                Span errorSpan = spanManager.createNodeSpanWithContext(
                        processInstanceId,
                        processInstance.getProcessId(),
                        processInstance.getProcessVersion(),
                        ProcessStates.ERROR,
                        NodeNames.PROCESS_EXECUTION,
                        extractedContext);

                if (errorSpan != null) {
                    String errorMessage = extractErrorMessage(processInstance);
                    String errorType = extractErrorType(processInstance);

                    Attributes errorEventAttributes = buildProcessErrorEventAttributes(
                            processInstanceId, errorMessage, errorType);
                    spanManager.addProcessEvent(errorSpan, PROCESS_INSTANCE_ERROR, errorEventAttributes);
                    LOGGER.debug("Added process.instance.error event for failed process {}", processInstanceId);

                    if (config.events().processEvents().includeComplete()) {
                        Attributes completeEventAttributes = buildProcessCompleteEventAttributes(
                                processInstanceId, durationMs, ProcessStates.ERROR);
                        spanManager.addProcessEvent(errorSpan, PROCESS_INSTANCE_COMPLETE, completeEventAttributes);
                        LOGGER.debug("Added process.instance.complete event for failed process {}", processInstanceId);
                    }

                    spanManager.setSpanError(errorSpan, null, ErrorConstants.SPAN_ERROR_DESCRIPTION);
                    errorSpan.end();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create error span for failed process {}: {}", processInstanceId, e.getMessage(), e);
            }
        }
    }

    public void handleProcessErrorEvent(Span span, String processInstanceId, KogitoProcessInstance processInstance) {
        try {
            String errorMessage = extractErrorMessage(processInstance);
            String errorType = extractErrorType(processInstance);

            Attributes errorEventAttributes = buildProcessErrorEventAttributes(
                    processInstanceId, errorMessage, errorType);
            spanManager.addProcessEvent(span, PROCESS_INSTANCE_ERROR, errorEventAttributes);
            LOGGER.debug("Added process.instance.error event for process instance {} (error: {}, type: {})",
                    processInstanceId, errorMessage, errorType);
        } catch (Exception e) {
            LOGGER.error("Failed to create process.instance.error event for {}: {}", processInstanceId, e.getMessage(), e);
        }
    }

    /**
     * Add process completion event to the specified span with direct process data.
     * This method works with direct data instead of relying on stored completion context,
     * eliminating race conditions in completion context handling.
     *
     * @param span the target span to add the event to
     * @param processInstanceId the process instance ID
     * @param durationMs the process duration in milliseconds
     * @param outcome the process outcome (COMPLETED, ERROR, ABORTED)
     */
    public void addProcessCompleteEvent(Span span, String processInstanceId, long durationMs, String outcome) {
        try {
            Attributes completeEventAttributes = buildProcessCompleteEventAttributes(
                    processInstanceId, durationMs, outcome);
            spanManager.addProcessEvent(span, PROCESS_INSTANCE_COMPLETE, completeEventAttributes);
            LOGGER.debug("Added process.instance.complete event for process instance {}", processInstanceId);
        } catch (Exception e) {
            LOGGER.error("Failed to create process.instance.complete event for {}: {}", processInstanceId, e.getMessage(), e);
        }
    }

    private Attributes buildProcessStartEventAttributes(String processInstanceId, String transactionId) {
        return Attributes.of(
                PROCESS_INSTANCE_ID, processInstanceId,
                TRIGGER, TriggerTypes.HTTP,
                REFERENCE_ID, transactionId);
    }

    private Attributes buildProcessCompleteEventAttributes(String processInstanceId, long durationMs, String outcome) {
        return Attributes.of(
                PROCESS_INSTANCE_ID, processInstanceId,
                OUTCOME, outcome,
                DURATION_MS, durationMs);
    }

    private Attributes buildProcessErrorEventAttributes(String processInstanceId, String errorMessage, String errorType) {
        return Attributes.of(
                PROCESS_INSTANCE_ID, processInstanceId,
                ERROR_MESSAGE, errorMessage != null ? errorMessage : ErrorConstants.UNKNOWN_ERROR,
                ERROR_TYPE, errorType != null ? errorType : ErrorConstants.PROCESS_EXECUTION_ERROR);
    }

    private String extractErrorMessage(KogitoProcessInstance processInstance) {
        try {
            org.kie.kogito.process.ProcessInstance<?> wrappedInstance = processInstance.unwrap();
            if (wrappedInstance != null && wrappedInstance.error().isPresent()) {
                String errorMessage = wrappedInstance.error().get().errorMessage();
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    return errorMessage;
                }
            }

            Map<String, Object> variables = processInstance.getVariables();
            if (variables != null) {
                Object errorInfo = variables.get(VariableNames.ERROR);
                if (errorInfo != null) {
                    return errorInfo.toString();
                }
            }

            return ErrorConstants.PROCESS_EXECUTION_FAILED_UNCAUGHT;
        } catch (Exception e) {
            LOGGER.debug("Could not extract error message from process instance {}: {}",
                    processInstance.getId(), e.getMessage());
            return ErrorConstants.PROCESS_EXECUTION_FAILED;
        }
    }

    private String extractErrorType(KogitoProcessInstance processInstance) {
        try {
            org.kie.kogito.process.ProcessInstance<?> wrappedInstance = processInstance.unwrap();
            if (wrappedInstance != null && wrappedInstance.error().isPresent()) {
                Throwable errorCause = wrappedInstance.error().get().errorCause();
                if (errorCause != null) {
                    return errorCause.getClass().getSimpleName();
                }
            }

            Map<String, Object> variables = processInstance.getVariables();
            if (variables != null) {
                Object errorInfo = variables.get(VariableNames.ERROR);
                if (errorInfo != null) {
                    return errorInfo.getClass().getSimpleName();
                }
            }

            return ErrorConstants.WORKFLOW_EXECUTION_EXCEPTION;
        } catch (Exception e) {
            LOGGER.debug("Could not extract error type from process instance {}: {}",
                    processInstance.getId(), e.getMessage());
            return ErrorConstants.PROCESS_EXECUTION_ERROR;
        }
    }
}

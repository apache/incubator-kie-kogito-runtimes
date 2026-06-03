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
package org.kie.kogito.addons.externalsignals.runtime;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.kie.kogito.addons.externalsignals.ExternalSignalDispatcher;
import org.kie.kogito.addons.externalsignals.ExternalSignalEvent;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Work item handler for processing external signal throw events.
 * 
 * This handler is automatically registered for work items with the name "External Send Task"
 * which are created when a BPMN signal throw event has an "external" scope.
 *
 * The handler extracts signal information from the work item parameters and dispatches
 * the signal to external systems via the configured {@link ExternalSignalDispatcher}.
 *
 * <b>Work Item Parameters:</b>
 * <ul>
 * <li><b>Signal</b> (required) - The name of the signal to send</li>
 * <li><b>Data</b> (optional) - The data payload to send with the signal</li>
 * <li><b>SignalProcessInstanceId</b> (optional) - Target process instance ID for the signal</li>
 * <li><b>SignalWorkItemId</b> (optional) - Target work item ID for the signal</li>
 * <li><b>SignalDeploymentId</b> (optional) - Target deployment ID for the signal</li>
 * </ul>
 * 
 * This handler operates in fire-and-forget mode. The signal is dispatched
 * and the work item is immediately completed without waiting for a response. Correlation
 * support for request-response patterns can be added as a follow-up if needed.
 *
 * @see ExternalSignalDispatcher
 * @see ExternalSignalEvent
 */
public class ExternalSignalWorkItemHandler extends DefaultKogitoWorkItemHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalSignalWorkItemHandler.class);

    /**
     * The name of this work item handler as registered in the process engine.
     */
    public static final String HANDLER_NAME = "External Send Task";

    private static final String PARAM_SIGNAL = "Signal";
    private static final String PARAM_DATA = "Data";
    private static final String PARAM_SIGNAL_PROCESS_INSTANCE_ID = "SignalProcessInstanceId";
    private static final String PARAM_SIGNAL_WORK_ITEM_ID = "SignalWorkItemId";
    private static final String PARAM_SIGNAL_DEPLOYMENT_ID = "SignalDeploymentId";

    private final ExternalSignalDispatcher dispatcher;

    public ExternalSignalWorkItemHandler() {
        this(new DefaultExternalSignalDispatcher());
    }

    /**
     * Creates a new handler with the specified dispatcher.
     * 
     * @param dispatcher the signal dispatcher to use
     * @throws IllegalArgumentException if dispatcher is null
     */
    public ExternalSignalWorkItemHandler(ExternalSignalDispatcher dispatcher) {
        super();
        if (dispatcher == null) {
            throw new IllegalArgumentException("ExternalSignalDispatcher cannot be null");
        }
        this.dispatcher = dispatcher;
        LOG.info("External signal work item handler initialized");
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(
            KogitoWorkItemManager manager,
            KogitoWorkItemHandler handler,
            KogitoWorkItem workItem,
            WorkItemTransition transition) {

        try {
            LOG.debug("Processing external signal work item: {} from process instance: {}",
                    workItem.getStringId(), workItem.getProcessInstanceId());

            // Extract signal metadata from work item parameters
            String signalName = extractSignalName(workItem);
            Object signalData = workItem.getParameter(PARAM_DATA);
            String processInstanceId = workItem.getProcessInstanceStringId();
            String correlationId = workItem.getStringId();

            // Extract optional targeting parameters
            String targetProcessInstanceId = (String) workItem.getParameter(PARAM_SIGNAL_PROCESS_INSTANCE_ID);
            String targetWorkItemId = (String) workItem.getParameter(PARAM_SIGNAL_WORK_ITEM_ID);
            String targetDeploymentId = (String) workItem.getParameter(PARAM_SIGNAL_DEPLOYMENT_ID);

            LOG.debug("Extracted signal: name='{}', correlationId='{}', sourceProcessInstance='{}'",
                    signalName, correlationId, processInstanceId);

            // Create external signal event
            ExternalSignalEvent event = ExternalSignalEvent.builder()
                    .signalName(signalName)
                    .signalData(signalData)
                    .sourceProcessInstanceId(processInstanceId)
                    .correlationId(correlationId)
                    .timestamp(Instant.now())
                    .build();

            // Add optional metadata
            if (targetProcessInstanceId != null) {
                event.addMetadata("targetProcessInstanceId", targetProcessInstanceId);
            }
            if (targetWorkItemId != null) {
                event.addMetadata("targetWorkItemId", targetWorkItemId);
            }
            if (targetDeploymentId != null) {
                event.addMetadata("targetDeploymentId", targetDeploymentId);
            }

            // Add work item metadata
            event.addMetadata("workItemId", workItem.getStringId());
            if (workItem.getNodeInstance() != null) {
                event.addMetadata("nodeInstanceId", workItem.getNodeInstance().getStringId());
                if (workItem.getNodeInstance().getNodeId() != null) {
                    event.addMetadata("nodeId", workItem.getNodeInstance().getNodeId().toString());
                }
            }

            // Dispatch to external system
            dispatcher.dispatch(event);

            LOG.info("Successfully dispatched external signal '{}' with correlation ID '{}' from process instance '{}'",
                    signalName, correlationId, processInstanceId);

            return Optional.of(this.workItemLifeCycle.newTransition("complete", workItem.getPhaseStatus(), Collections.emptyMap()));

        } catch (IllegalArgumentException e) {
            LOG.error("Invalid work item parameters for external signal: {}", e.getMessage(), e);
            // Abort the work item on validation errors
            return Optional.of(this.workItemLifeCycle.newTransition("abort", workItem.getPhaseStatus(), Collections.emptyMap()));

        } catch (Exception e) {
            LOG.error("Error processing external signal work item: {}", workItem.getStringId(), e);
            // Abort the work item on dispatch errors
            return Optional.of(this.workItemLifeCycle.newTransition("abort", workItem.getPhaseStatus(), Collections.emptyMap()));
        }
    }

    /**
     * Extracts and validates the signal name from the work item.
     * 
     * @param workItem the work item
     * @return the signal name
     * @throws IllegalArgumentException if signal name is missing or invalid
     */
    private String extractSignalName(KogitoWorkItem workItem) {
        Object signalParam = workItem.getParameter(PARAM_SIGNAL);

        if (signalParam == null) {
            throw new IllegalArgumentException(
                    "Required parameter '" + PARAM_SIGNAL + "' is missing from work item");
        }

        if (!(signalParam instanceof String)) {
            throw new IllegalArgumentException(
                    "Parameter '" + PARAM_SIGNAL + "' must be a String, but was: " +
                            signalParam.getClass().getName());
        }

        String signalName = ((String) signalParam).trim();

        if (signalName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Parameter '" + PARAM_SIGNAL + "' cannot be empty");
        }

        return signalName;
    }

    @Override
    public String toString() {
        return "ExternalSignalWorkItemHandler{" +
                "name='" + HANDLER_NAME + '\'' +
                ", dispatcher=" + dispatcher.getClass().getSimpleName() +
                '}';
    }
}

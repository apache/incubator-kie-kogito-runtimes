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
package org.kie.kogito.addons.externalsignals;

/**
 * Interface for dispatching external signals to external systems.
 * 
 * Implementations of this interface are responsible for:
 * <ul>
 * <li>Resolving the target topic/trigger for a signal based on configuration</li>
 * <li>Transforming the signal event into the appropriate format</li>
 * <li>Dispatching the signal to the external messaging infrastructure</li>
 * <li>Handling dispatch errors appropriately</li>
 * </ul>
 * 
 * The dispatcher uses Kogito's {@link org.kie.kogito.event.EventEmitter} infrastructure
 * to remain transport-agnostic. The actual transport (Kafka, HTTP, etc.) is determined
 * by the EventEmitter implementation available at runtime.
 *
 * Example usage:
 *
 * <pre>
 * {
 *     &#64;code
 *     ExternalSignalDispatcher dispatcher = new DefaultExternalSignalDispatcher(config);
 * 
 *     ExternalSignalEvent event = ExternalSignalEvent.builder()
 *             .signalName("OrderApproval")
 *             .signalData(orderData)
 *             .sourceProcessInstanceId("order-123")
 *             .build();
 * 
 *     dispatcher.dispatch(event);
 * }
 * </pre>
 * 
 * @see ExternalSignalEvent
 * @see ExternalSignalConfig
 */
public interface ExternalSignalDispatcher {

    /**
     * Dispatches an external signal event to the appropriate external system.
     * 
     * The dispatcher will:
     * <ol>
     * <li>Resolve the target topic/trigger based on the signal name and configuration</li>
     * <li>Transform the event into a CloudEvent-compatible format</li>
     * <li>Emit the event using the appropriate EventEmitter</li>
     * </ol>
     * 
     * This method operates in a fire-and-forget manner for Phase 1. The signal
     * is dispatched asynchronously and the method returns immediately without
     * waiting for acknowledgment from the external system.
     *
     * @param event the external signal event to dispatch
     * @throws IllegalArgumentException if the event is null or invalid
     * @throws ExternalSignalDispatchException if the dispatch fails
     */
    void dispatch(ExternalSignalEvent event);

    /**
     * Resolves the target topic or trigger name for a given signal name.
     * 
     * The resolution follows this priority:
     * <ol>
     * <li>Explicit mapping from configuration (kogito.external-signals.mapping.{signalName})</li>
     * <li>Default naming convention: {prefix}-{signalName}</li>
     * </ol>
     * 
     * @param signalName the name of the signal
     * @return the resolved topic/trigger name
     * @throws IllegalArgumentException if signalName is null or empty
     */
    String resolveTrigger(String signalName);
}

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

import java.util.Map;
import java.util.Optional;

/**
 * Configuration interface for external signal dispatching.
 * 
 * This interface defines the configuration options for controlling how external
 * signals are dispatched to external systems. Implementations can load configuration
 * from various sources (properties files, environment variables, etc.).
 *
 * Configuration properties:
 *
 * <ul>
 * <li><b>kogito.external-signals.mapping.{signalName}</b> - Maps a signal name to a specific topic/trigger</li>
 * <li><b>kogito.external-signals.default-prefix</b> - Default prefix for auto-generated topic names (default: "kogito-external-signal")</li>
 * </ul>
 * 
 * Example configuration:
 *
 * <pre>
 * kogito.external-signals.mapping.OrderCreated=order-events
 * kogito.external-signals.mapping.PaymentProcessed=payment-topic
 * kogito.external-signals.default-prefix=my-app-signal
 * </pre>
 * 
 * @see ExternalSignalDispatcher
 */
public interface ExternalSignalConfig {

    String DEFAULT_TRIGGER_PREFIX = "kogito-external-signal";

    /**
     * Gets the explicit mapping of signal names to topics/triggers.
     * 
     * This map contains user-defined mappings from signal names to their target
     * topics or triggers. If a signal name is not found in this map, the dispatcher
     * will use the default naming convention.
     *
     * @return a map of signal names to topic/trigger names, never null
     */
    Map<String, String> getSignalTriggerMapping();

    /**
     * Gets the default prefix used for auto-generated topic names.
     * 
     * When a signal name is not explicitly mapped, the topic name is generated as:
     * {@code {prefix}-{signalName}}
     *
     * @return the default prefix, never null
     */
    String getDefaultTriggerPrefix();

    /**
     * Gets the mapped topic/trigger for a specific signal name.
     * 
     * This is a convenience method that checks the explicit mappings first,
     * and returns empty if no mapping exists.
     *
     * @param signalName the signal name to look up
     * @return the mapped topic/trigger, or empty if no explicit mapping exists
     */
    default Optional<String> getMappedTrigger(String signalName) {
        return Optional.ofNullable(getSignalTriggerMapping().get(signalName));
    }

    /**
     * Resolves the complete topic/trigger name for a signal.
     * 
     * Resolution logic:
     * <ol>
     * <li>If an explicit mapping exists, use it</li>
     * <li>Otherwise, generate: {defaultPrefix}-{signalName}</li>
     * </ol>
     * 
     * @param signalName the signal name
     * @return the resolved topic/trigger name
     */
    default String resolveTrigger(String signalName) {
        return getMappedTrigger(signalName)
                .orElse(getDefaultTriggerPrefix() + "-" + signalName);
    }
}

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.kie.kogito.addons.externalsignals.ExternalSignalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link ExternalSignalConfig} that loads configuration
 * from system properties or provided properties.
 * 
 * This implementation supports the following configuration properties:
 * <ul>
 * <li><b>kogito.external-signals.mapping.{signalName}</b> - Maps a signal name to a topic/trigger</li>
 * <li><b>kogito.external-signals.default-prefix</b> - Sets the default prefix for topic names</li>
 * </ul>
 * 
 * Example:
 *
 * <pre>
 * kogito.external-signals.mapping.OrderCreated=order-events
 * kogito.external-signals.mapping.PaymentProcessed=payment-topic
 * kogito.external-signals.default-prefix=my-app-signal
 * </pre>
 */
public class ExternalSignalConfigImpl implements ExternalSignalConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalSignalConfigImpl.class);

    private static final String MAPPING_PREFIX = "kogito.external-signals.mapping.";
    private static final String DEFAULT_PREFIX_PROPERTY = "kogito.external-signals.default-prefix";

    private final Map<String, String> signalTriggerMapping;
    private final String defaultTriggerPrefix;

    /**
     * Creates a new configuration instance using system properties.
     */
    public ExternalSignalConfigImpl() {
        this(System.getProperties());
    }

    /**
     * Creates a new configuration instance from the provided properties.
     * 
     * @param properties the properties to load configuration from
     */
    public ExternalSignalConfigImpl(Properties properties) {
        this(propertiesToMap(properties));
    }

    /**
     * Creates a new configuration instance from the provided map.
     * 
     * @param properties the properties map to load configuration from
     */
    public ExternalSignalConfigImpl(Map<String, String> properties) {
        this.signalTriggerMapping = loadSignalMappings(properties);
        this.defaultTriggerPrefix = loadDefaultPrefix(properties);

        LOG.info("External signal configuration loaded: {} signal mappings, default prefix: '{}'",
                signalTriggerMapping.size(), defaultTriggerPrefix);

        if (LOG.isDebugEnabled() && !signalTriggerMapping.isEmpty()) {
            signalTriggerMapping.forEach((signal, trigger) -> LOG.debug("Signal mapping: {} -> {}", signal, trigger));
        }
    }

    @Override
    public Map<String, String> getSignalTriggerMapping() {
        return Collections.unmodifiableMap(signalTriggerMapping);
    }

    @Override
    public String getDefaultTriggerPrefix() {
        return defaultTriggerPrefix;
    }

    /**
     * Loads signal-to-trigger mappings from properties.
     * 
     * @param properties the properties map
     * @return the signal mappings
     */
    private Map<String, String> loadSignalMappings(Map<String, String> properties) {
        Map<String, String> mappings = new HashMap<>();

        properties.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(MAPPING_PREFIX))
                .forEach(entry -> {
                    String signalName = entry.getKey().substring(MAPPING_PREFIX.length());
                    String trigger = entry.getValue();

                    if (signalName.isEmpty()) {
                        LOG.warn("Ignoring invalid signal mapping with empty signal name");
                        return;
                    }

                    if (trigger == null || trigger.trim().isEmpty()) {
                        LOG.warn("Ignoring signal mapping for '{}' with empty trigger", signalName);
                        return;
                    }

                    mappings.put(signalName, trigger.trim());
                    LOG.debug("Loaded signal mapping: {} -> {}", signalName, trigger.trim());
                });

        return mappings;
    }

    /**
     * Loads the default trigger prefix from properties.
     * 
     * @param properties the properties map
     * @return the default prefix
     */
    private String loadDefaultPrefix(Map<String, String> properties) {
        String prefix = properties.get(DEFAULT_PREFIX_PROPERTY);

        if (prefix != null && !prefix.trim().isEmpty()) {
            LOG.debug("Using configured default prefix: '{}'", prefix.trim());
            return prefix.trim();
        }

        LOG.debug("Using default prefix: '{}'", DEFAULT_TRIGGER_PREFIX);
        return DEFAULT_TRIGGER_PREFIX;
    }

    /**
     * Converts Properties to a Map<String, String>.
     * 
     * @param properties the properties
     * @return the map
     */
    private static Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new HashMap<>();
        properties.stringPropertyNames().forEach(key -> map.put(key, properties.getProperty(key)));
        return map;
    }

    @Override
    public String toString() {
        return "ExternalSignalConfigImpl{" +
                "mappings=" + signalTriggerMapping.size() +
                ", defaultPrefix='" + defaultTriggerPrefix + '\'' +
                '}';
    }
}

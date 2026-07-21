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

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.kie.kogito.addons.externalsignals.ExternalSignalConfig;
import org.kie.kogito.addons.externalsignals.ExternalSignalDispatchException;
import org.kie.kogito.addons.externalsignals.ExternalSignalDispatcher;
import org.kie.kogito.addons.externalsignals.ExternalSignalEvent;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.impl.EventFactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;

/**
 * Default implementation of {@link ExternalSignalDispatcher} that uses Kogito's
 * {@link EventEmitter} infrastructure for transport-agnostic signal dispatching.
 * 
 * This dispatcher:
 * <ul>
 * <li>Resolves the target topic/trigger based on configuration</li>
 * <li>Transforms the signal event into a CloudEvent-compatible DataEvent</li>
 * <li>Dispatches the event using the appropriate EventEmitter</li>
 * <li>Operates in fire-and-forget mode (Phase 1)</li>
 * </ul>
 * 
 * The actual transport mechanism (Kafka, HTTP, etc.) is determined by the
 * EventEmitter implementation available at runtime through ServiceLoader.
 *
 * @see ExternalSignalEvent
 * @see ExternalSignalConfig
 * @see EventEmitter
 */
public class DefaultExternalSignalDispatcher implements ExternalSignalDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExternalSignalDispatcher.class);

    private static final String EVENT_TYPE_PREFIX = "org.kie.kogito.signal.external";

    private final ExternalSignalConfig config;

    public DefaultExternalSignalDispatcher() {
        this(new ExternalSignalConfigImpl());
    }

    /**
     * Creates a new dispatcher with the specified configuration.
     * 
     * @param config the configuration to use
     * @throws IllegalArgumentException if config is null
     */
    public DefaultExternalSignalDispatcher(ExternalSignalConfig config) {
        this.config = Objects.requireNonNull(config, "ExternalSignalConfig cannot be null");
        LOG.info("External signal dispatcher initialized with config: {}", config);
    }

    @Override
    public void dispatch(ExternalSignalEvent event) {
        Objects.requireNonNull(event, "ExternalSignalEvent cannot be null");

        if (event.getSignalName() == null || event.getSignalName().trim().isEmpty()) {
            throw new IllegalArgumentException("Signal name cannot be null or empty");
        }

        try {
            String trigger = resolveTrigger(event.getSignalName());

            LOG.debug("Dispatching external signal '{}' to trigger '{}' from process instance '{}'",
                    event.getSignalName(), trigger, event.getSourceProcessInstanceId());

            // Get the appropriate event emitter for this trigger
            EventEmitter emitter = EventFactoryUtils.getEventEmitter(trigger);

            // Transform to DataEvent
            DataEvent<ExternalSignalEvent> dataEvent = createDataEvent(event, trigger);

            // Emit the event
            emitter.emit(dataEvent);

            LOG.info("Successfully dispatched external signal '{}' with correlation ID '{}' to trigger '{}'",
                    event.getSignalName(), event.getCorrelationId(), trigger);

        } catch (Exception e) {
            String errorMsg = String.format(
                    "Failed to dispatch external signal '%s' from process instance '%s'",
                    event.getSignalName(), event.getSourceProcessInstanceId());
            LOG.error(errorMsg, e);
            throw new ExternalSignalDispatchException(errorMsg, e);
        }
    }

    @Override
    public String resolveTrigger(String signalName) {
        if (signalName == null || signalName.trim().isEmpty()) {
            throw new IllegalArgumentException("Signal name cannot be null or empty");
        }

        String trigger = config.resolveTrigger(signalName);
        LOG.debug("Resolved signal '{}' to trigger '{}'", signalName, trigger);
        return trigger;
    }

    /**
     * Creates a CloudEvent-compatible DataEvent from an ExternalSignalEvent.
     * 
     * @param event the external signal event
     * @param trigger the resolved trigger name
     * @return the data event
     */
    private DataEvent<ExternalSignalEvent> createDataEvent(ExternalSignalEvent event, String trigger) {
        // Create event type: org.kie.kogito.signal.external.{signalName}
        String eventType = EVENT_TYPE_PREFIX + "." + event.getSignalName();

        // Create source URI
        URI source = event.getSource();

        // Create the DataEvent using the factory
        return new ExternalSignalDataEvent(
                eventType,
                source,
                event,
                event.getCorrelationId());
    }

    /**
     * Simple DataEvent wrapper for external signals.
     * This is a minimal implementation for Phase 1.
     */
    private static class ExternalSignalDataEvent implements DataEvent<ExternalSignalEvent> {

        private final String type;
        private final URI source;
        private final ExternalSignalEvent data;
        private final String id;

        ExternalSignalDataEvent(String type, URI source, ExternalSignalEvent data, String id) {
            this.type = type;
            this.source = source;
            this.data = data;
            this.id = id;
        }

        @Override
        public ExternalSignalEvent getData() {
            return data;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public URI getSource() {
            return source;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public SpecVersion getSpecVersion() {
            return SpecVersion.V1;
        }

        @Override
        public OffsetDateTime getTime() {
            return data.getTimestamp() != null ? OffsetDateTime.now() : null;
        }

        @Override
        public URI getDataSchema() {
            return null;
        }

        @Override
        public String getDataContentType() {
            return "application/json";
        }

        @Override
        public String getSubject() {
            return null;
        }

        @Override
        public Object getAttribute(String attributeName) throws IllegalArgumentException {
            switch (attributeName) {
                case "id":
                    return getId();
                case "source":
                    return getSource();
                case "specversion":
                    return getSpecVersion();
                case "type":
                    return getType();
                case "datacontenttype":
                    return getDataContentType();
                case "dataschema":
                    return getDataSchema();
                case "subject":
                    return getSubject();
                case "time":
                    return getTime();
                default:
                    return null;
            }
        }

        @Override
        public Object getExtension(String extensionName) {
            return null;
        }

        @Override
        public Set<String> getExtensionNames() {
            return java.util.Collections.emptySet();
        }

        @Override
        public String getKogitoProcessInstanceId() {
            return data.getSourceProcessInstanceId();
        }

        @Override
        public String getKogitoRootProcessInstanceId() {
            return null;
        }

        @Override
        public String getKogitoProcessId() {
            return null;
        }

        @Override
        public String getKogitoRootProcessId() {
            return null;
        }

        @Override
        public String getKogitoAddons() {
            return null;
        }

        @Override
        public String getKogitoParentProcessInstanceId() {
            return null;
        }

        @Override
        public String getKogitoProcessInstanceState() {
            return null;
        }

        @Override
        public String getKogitoReferenceId() {
            return null;
        }

        @Override
        public String getKogitoBusinessKey() {
            return null;
        }

        @Override
        public String getKogitoStartFromNode() {
            return null;
        }

        @Override
        public String getKogitoProcessInstanceVersion() {
            return null;
        }

        @Override
        public String getKogitoProcessType() {
            return null;
        }

        @Override
        public String getKogitoIdentity() {
            return null;
        }

        @Override
        public CloudEvent asCloudEvent(Function<ExternalSignalEvent, CloudEventData> mapper) {
            return CloudEventBuilder.v1()
                    .withId(getId())
                    .withSource(getSource())
                    .withType(getType())
                    .withTime(getTime())
                    .withDataContentType(getDataContentType())
                    .withData(mapper.apply(getData()))
                    .withExtension("kogitoProcessInstanceId", getKogitoProcessInstanceId())
                    .build();
        }

        @Override
        public String toString() {
            return "ExternalSignalDataEvent{" +
                    "type='" + type + '\'' +
                    ", source=" + source +
                    ", id='" + id + '\'' +
                    ", signalName='" + data.getSignalName() + '\'' +
                    '}';
        }
    }
}

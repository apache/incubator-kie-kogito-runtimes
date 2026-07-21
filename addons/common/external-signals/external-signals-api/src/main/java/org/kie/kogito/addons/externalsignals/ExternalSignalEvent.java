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

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an external signal event that is dispatched outside the process engine scope.
 * This event model is CloudEvent-compatible and contains all necessary metadata for
 * signal correlation and routing.
 * 
 * External signals are used when a BPMN signal throw event has an "external" scope,
 * indicating that the signal should be sent to an external system via messaging infrastructure
 * (e.g., Kafka, HTTP) rather than being processed internally by the process engine.
 * 
 * <pre>
 * {
 *     &#64;code
 *     ExternalSignalEvent event = ExternalSignalEvent.builder()
 *             .signalName("OrderApproval")
 *             .signalData(orderData)
 *             .sourceProcessInstanceId("order-123")
 *             .correlationId("signal-456")
 *             .build();
 * }
 * </pre>
 * 
 * @see ExternalSignalDispatcher
 */
public class ExternalSignalEvent {

    private String signalName;
    private Object signalData;
    private String sourceProcessInstanceId;
    private String correlationId;
    private Instant timestamp;
    private Map<String, Object> metadata;

    public ExternalSignalEvent() {
        this.metadata = new HashMap<>();
        this.timestamp = Instant.now();
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public Object getSignalData() {
        return signalData;
    }

    public void setSignalData(Object signalData) {
        this.signalData = signalData;
    }

    public String getSourceProcessInstanceId() {
        return sourceProcessInstanceId;
    }

    public void setSourceProcessInstanceId(String sourceProcessInstanceId) {
        this.sourceProcessInstanceId = sourceProcessInstanceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    public String getType() {
        return "org.kie.kogito.signal.external";
    }

    public URI getSource() {
        return URI.create("kogito://process/" +
                (sourceProcessInstanceId != null ? sourceProcessInstanceId : "unknown"));
    }

    public String getId() {
        return correlationId;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExternalSignalEvent that = (ExternalSignalEvent) o;
        return Objects.equals(signalName, that.signalName) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(sourceProcessInstanceId, that.sourceProcessInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signalName, correlationId, sourceProcessInstanceId);
    }

    @Override
    public String toString() {
        return "ExternalSignalEvent{" +
                "signalName='" + signalName + '\'' +
                ", sourceProcessInstanceId='" + sourceProcessInstanceId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public static class Builder {
        private final ExternalSignalEvent event;

        private Builder() {
            this.event = new ExternalSignalEvent();
        }

        public Builder signalName(String signalName) {
            event.setSignalName(signalName);
            return this;
        }

        public Builder signalData(Object signalData) {
            event.setSignalData(signalData);
            return this;
        }

        public Builder sourceProcessInstanceId(String sourceProcessInstanceId) {
            event.setSourceProcessInstanceId(sourceProcessInstanceId);
            return this;
        }

        public Builder correlationId(String correlationId) {
            event.setCorrelationId(correlationId);
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            event.setTimestamp(timestamp);
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            event.setMetadata(metadata);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            event.addMetadata(key, value);
            return this;
        }

        public ExternalSignalEvent build() {
            return event;
        }
    }
}

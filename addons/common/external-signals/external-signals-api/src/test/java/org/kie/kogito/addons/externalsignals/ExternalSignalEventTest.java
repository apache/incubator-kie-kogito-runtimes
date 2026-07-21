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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalSignalEventTest {

    @Test
    public void testBuilderCreatesEventWithAllFields() {
        // Given
        String signalName = "OrderApproval";
        Object signalData = Map.of("orderId", "12345");
        String processInstanceId = "pid-123";
        String correlationId = "correlationID456";
        Instant timestamp = Instant.now();
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key1", "value1");

        // When
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName(signalName)
                .signalData(signalData)
                .sourceProcessInstanceId(processInstanceId)
                .correlationId(correlationId)
                .timestamp(timestamp)
                .metadata(metadata)
                .build();

        // Then
        assertThat(event.getSignalName()).isEqualTo(signalName);
        assertThat(event.getSignalData()).isEqualTo(signalData);
        assertThat(event.getSourceProcessInstanceId()).isEqualTo(processInstanceId);
        assertThat(event.getCorrelationId()).isEqualTo(correlationId);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getMetadata()).containsEntry("key1", "value1");
    }

    @Test
    public void testBuilderWithAddMetadata() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .addMetadata("key1", "value1")
                .addMetadata("key2", "value2")
                .build();

        assertThat(event.getMetadata())
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value2");
    }

    @Test
    public void testCloudEventCompatibility() {
        String processInstanceId = "pid-123";
        String correlationId = "correlationID456";

        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .sourceProcessInstanceId(processInstanceId)
                .correlationId(correlationId)
                .build();

        assertThat(event.getType()).isEqualTo("org.kie.kogito.signal.external");
        assertThat(event.getSource()).isEqualTo(URI.create("kogito://process/" + processInstanceId));
        assertThat(event.getId()).isEqualTo(correlationId);
    }

    @Test
    public void testCloudEventSourceWithNullProcessInstanceId() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .build();

        assertThat(event.getSource()).isEqualTo(URI.create("kogito://process/unknown"));
    }

    @Test
    public void testDefaultConstructorInitializesMetadata() {
        ExternalSignalEvent event = new ExternalSignalEvent();

        assertThat(event.getMetadata()).isNotNull().isEmpty();
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    public void testAddMetadataMethod() {
        ExternalSignalEvent event = new ExternalSignalEvent();

        event.addMetadata("key1", "value1");
        event.addMetadata("key2", 123);

        assertThat(event.getMetadata())
                .containsEntry("key1", "value1")
                .containsEntry("key2", 123);
    }

    @Test
    public void testSetMetadataWithNull() {
        ExternalSignalEvent event = new ExternalSignalEvent();
        event.addMetadata("key1", "value1");

        event.setMetadata(null);

        assertThat(event.getMetadata()).isNotNull().isEmpty();
    }

    @Test
    public void testEqualsAndHashCode() {
        ExternalSignalEvent event1 = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .sourceProcessInstanceId("pid-123")
                .correlationId("correlationID456")
                .build();

        ExternalSignalEvent event2 = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .sourceProcessInstanceId("pid-123")
                .correlationId("correlationID456")
                .build();

        ExternalSignalEvent event3 = ExternalSignalEvent.builder()
                .signalName("DifferentSignal")
                .sourceProcessInstanceId("pid-123")
                .correlationId("correlationID456")
                .build();

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1).isNotEqualTo(event3);
    }

    @Test
    public void testToString() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .sourceProcessInstanceId("pid-123")
                .correlationId("correlationID456")
                .build();

        String toString = event.toString();

        assertThat(toString)
                .contains("TestSignal")
                .contains("pid-123")
                .contains("correlationID456");
    }
}

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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.addons.externalsignals.ExternalSignalConfig;
import org.kie.kogito.addons.externalsignals.ExternalSignalDispatchException;
import org.kie.kogito.addons.externalsignals.ExternalSignalEvent;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventEmitter;
import org.kie.kogito.event.impl.EventFactoryUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultExternalSignalDispatcherTest {

    @Mock
    private ExternalSignalConfig config;

    @Mock
    private EventEmitter eventEmitter;

    private DefaultExternalSignalDispatcher dispatcher;

    @BeforeEach
    public void setUp() {
        dispatcher = new DefaultExternalSignalDispatcher(config);
    }

    @Test
    public void testConstructorWithNullConfig() {
        assertThatThrownBy(() -> new DefaultExternalSignalDispatcher(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ExternalSignalConfig cannot be null");
    }

    @Test
    public void testDefaultConstructor() {
        DefaultExternalSignalDispatcher defaultDispatcher = new DefaultExternalSignalDispatcher();

        assertThat(defaultDispatcher).isNotNull();
    }

    @Test
    public void testSuccessfulDispatchWithMappedTrigger() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("OrderCreated")
                .signalData(Map.of("orderId", "123"))
                .sourceProcessInstanceId("pid-001")
                .correlationId("correlationID-001")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("OrderCreated")).thenReturn("order-events");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("order-events"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            assertThat(capturedEvent.getType()).isEqualTo("org.kie.kogito.signal.external.OrderCreated");
            assertThat(capturedEvent.getId()).isEqualTo("correlationID-001");
            assertThat(capturedEvent.getSource()).isEqualTo(URI.create("kogito://process/pid-001"));
            assertThat(capturedEvent.getData()).isInstanceOf(ExternalSignalEvent.class);

            ExternalSignalEvent eventData = (ExternalSignalEvent) capturedEvent.getData();
            assertThat(eventData.getSignalName()).isEqualTo("OrderCreated");
            assertThat(eventData.getSignalData()).isEqualTo(Map.of("orderId", "123"));
        }
    }

    @Test
    public void testSuccessfulDispatchWithDefaultTrigger() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("CustomSignal")
                .signalData(Map.of("key", "value"))
                .sourceProcessInstanceId("pid-002")
                .correlationId("correlationID-002")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("CustomSignal")).thenReturn("kogito-external-signal-CustomSignal");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("kogito-external-signal-CustomSignal"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            assertThat(capturedEvent.getType()).isEqualTo("org.kie.kogito.signal.external.CustomSignal");
            assertThat(capturedEvent.getId()).isEqualTo("correlationID-002");
        }
    }

    @Test
    public void testDispatchWithNullEvent() {
        assertThatThrownBy(() -> dispatcher.dispatch(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ExternalSignalEvent cannot be null");
    }

    @Test
    public void testDispatchWithNullSignalName() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName(null)
                .signalData(Map.of("key", "value"))
                .sourceProcessInstanceId("pid-003")
                .correlationId("correlationID-003")
                .build();

        assertThatThrownBy(() -> dispatcher.dispatch(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Signal name cannot be null or empty");
    }

    @Test
    public void testDispatchWithEmptySignalName() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("   ")
                .signalData(Map.of("key", "value"))
                .sourceProcessInstanceId("pid-004")
                .correlationId("correlationID-004")
                .build();

        assertThatThrownBy(() -> dispatcher.dispatch(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Signal name cannot be null or empty");
    }

    @Test
    public void testDispatchWithNullData() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("NullDataSignal")
                .signalData(null)
                .sourceProcessInstanceId("pid-005")
                .correlationId("correlationID-005")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("NullDataSignal")).thenReturn("null-data-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("null-data-topic"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            ExternalSignalEvent eventData = (ExternalSignalEvent) capturedEvent.getData();
            assertThat(eventData.getSignalData()).isNull();
        }
    }

    @Test
    public void testDispatchWithMetadata() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("MetadataSignal")
                .signalData(Map.of("data", "value"))
                .sourceProcessInstanceId("pid-006")
                .correlationId("correlationID-006")
                .timestamp(Instant.now())
                .addMetadata("targetProcessInstanceId", "target-001")
                .addMetadata("targetWorkItemId", "workitem-001")
                .addMetadata("customKey", "customValue")
                .build();

        when(config.resolveTrigger("MetadataSignal")).thenReturn("metadata-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("metadata-topic"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            ExternalSignalEvent eventData = (ExternalSignalEvent) capturedEvent.getData();

            assertThat(eventData.getMetadata()).containsEntry("targetProcessInstanceId", "target-001");
            assertThat(eventData.getMetadata()).containsEntry("targetWorkItemId", "workitem-001");
            assertThat(eventData.getMetadata()).containsEntry("customKey", "customValue");
        }
    }

    @Test
    public void testDispatchWhenEventEmitterThrowsException() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("ErrorSignal")
                .signalData(Map.of("error", "test"))
                .sourceProcessInstanceId("pid-007")
                .correlationId("correlationID-007")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("ErrorSignal")).thenReturn("error-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("error-topic"))
                    .thenReturn(eventEmitter);

            doThrow(new RuntimeException("Emission failed"))
                    .when(eventEmitter).emit(any(DataEvent.class));

            assertThatThrownBy(() -> dispatcher.dispatch(event))
                    .isInstanceOf(ExternalSignalDispatchException.class)
                    .hasMessageContaining("Failed to dispatch external signal 'ErrorSignal'")
                    .hasMessageContaining("pid-007")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Test
    public void testDispatchWhenEventEmitterNotAvailable() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("NoEmitterSignal")
                .signalData(Map.of("data", "value"))
                .sourceProcessInstanceId("pid-008")
                .correlationId("correlationID-008")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("NoEmitterSignal")).thenReturn("no-emitter-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("no-emitter-topic"))
                    .thenThrow(new IllegalStateException("No EventEmitter available"));

            assertThatThrownBy(() -> dispatcher.dispatch(event))
                    .isInstanceOf(ExternalSignalDispatchException.class)
                    .hasMessageContaining("Failed to dispatch external signal 'NoEmitterSignal'")
                    .hasCauseInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void testResolveTriggerWithMappedSignal() {
        when(config.resolveTrigger("OrderCreated")).thenReturn("order-events");

        String trigger = dispatcher.resolveTrigger("OrderCreated");

        assertThat(trigger).isEqualTo("order-events");
        verify(config).resolveTrigger("OrderCreated");
    }

    @Test
    public void testResolveTriggerWithUnmappedSignal() {
        when(config.resolveTrigger("CustomSignal")).thenReturn("kogito-external-signal-CustomSignal");

        String trigger = dispatcher.resolveTrigger("CustomSignal");

        assertThat(trigger).isEqualTo("kogito-external-signal-CustomSignal");
        verify(config).resolveTrigger("CustomSignal");
    }

    @Test
    public void testResolveTriggerWithNullSignalName() {
        assertThatThrownBy(() -> dispatcher.resolveTrigger(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Signal name cannot be null or empty");
    }

    @Test
    public void testResolveTriggerWithEmptySignalName() {
        assertThatThrownBy(() -> dispatcher.resolveTrigger("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Signal name cannot be null or empty");
    }

    @Test
    public void testCloudEventTypeGeneration() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("PaymentProcessed")
                .signalData(Map.of("paymentId", "PAY-123"))
                .sourceProcessInstanceId("pid-009")
                .correlationId("correlationID-009")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("PaymentProcessed")).thenReturn("payment-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("payment-topic"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            assertThat(capturedEvent.getType()).isEqualTo("org.kie.kogito.signal.external.PaymentProcessed");
        }
    }

    @Test
    public void testCloudEventSourceGeneration() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .signalData(null)
                .sourceProcessInstanceId("my-pid-instance-123")
                .correlationId("correlationID-010")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("TestSignal")).thenReturn("test-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("test-topic"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            assertThat(capturedEvent.getSource()).isEqualTo(URI.create("kogito://process/my-pid-instance-123"));
        }
    }

    @Test
    public void testCloudEventIdMatchesCorrelationId() {
        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("TestSignal")
                .signalData(null)
                .sourceProcessInstanceId("pid-011")
                .correlationId("unique-correlation-id-456")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("TestSignal")).thenReturn("test-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("test-topic"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            assertThat(capturedEvent.getId()).isEqualTo("unique-correlation-id-456");
        }
    }

    @Test
    public void testComplexDataPayloadPreservation() {
        Map<String, Object> complexData = new HashMap<>();
        complexData.put("customer", Map.of("id", "CUST-123", "name", "John Doe"));
        complexData.put("items", java.util.Arrays.asList(
                Map.of("sku", "ITEM-1", "quantity", 2),
                Map.of("sku", "ITEM-2", "quantity", 1)));
        complexData.put("total", 299.99);

        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("OrderPlaced")
                .signalData(complexData)
                .sourceProcessInstanceId("pid-012")
                .correlationId("correlationID-012")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("OrderPlaced")).thenReturn("order-topic");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("order-topic"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event);

            ArgumentCaptor<DataEvent> dataEventCaptor = ArgumentCaptor.forClass(DataEvent.class);
            verify(eventEmitter).emit(dataEventCaptor.capture());

            DataEvent<?> capturedEvent = dataEventCaptor.getValue();
            ExternalSignalEvent eventData = (ExternalSignalEvent) capturedEvent.getData();
            assertThat(eventData.getSignalData()).isEqualTo(complexData);
        }
    }

    @Test
    public void testMultipleDispatchCalls() {
        ExternalSignalEvent event1 = ExternalSignalEvent.builder()
                .signalName("Signal1")
                .signalData(Map.of("id", "1"))
                .sourceProcessInstanceId("pid-013")
                .correlationId("correlationID-013")
                .timestamp(Instant.now())
                .build();

        ExternalSignalEvent event2 = ExternalSignalEvent.builder()
                .signalName("Signal2")
                .signalData(Map.of("id", "2"))
                .sourceProcessInstanceId("pid-014")
                .correlationId("correlationID-014")
                .timestamp(Instant.now())
                .build();

        when(config.resolveTrigger("Signal1")).thenReturn("topic1");
        when(config.resolveTrigger("Signal2")).thenReturn("topic2");

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("topic1"))
                    .thenReturn(eventEmitter);
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("topic2"))
                    .thenReturn(eventEmitter);

            dispatcher.dispatch(event1);
            dispatcher.dispatch(event2);

            verify(eventEmitter, times(2)).emit(any(DataEvent.class));
        }
    }

    @Test
    public void testDispatchWithConfigurationFromMap() {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("kogito.external-signals.mapping.OrderCreated", "order-events");
        configMap.put("kogito.external-signals.default-prefix", "my-app-signal");

        ExternalSignalConfig mapConfig = new ExternalSignalConfigImpl(configMap);
        DefaultExternalSignalDispatcher mapDispatcher = new DefaultExternalSignalDispatcher(mapConfig);

        ExternalSignalEvent event = ExternalSignalEvent.builder()
                .signalName("OrderCreated")
                .signalData(Map.of("orderId", "123"))
                .sourceProcessInstanceId("pid-015")
                .correlationId("correlationID-015")
                .timestamp(Instant.now())
                .build();

        try (MockedStatic<EventFactoryUtils> mockedFactory = mockStatic(EventFactoryUtils.class)) {
            mockedFactory.when(() -> EventFactoryUtils.getEventEmitter("order-events"))
                    .thenReturn(eventEmitter);

            mapDispatcher.dispatch(event);

            verify(eventEmitter).emit(any(DataEvent.class));
        }
    }
}

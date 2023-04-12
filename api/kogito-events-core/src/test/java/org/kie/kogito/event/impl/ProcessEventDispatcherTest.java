/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.event.impl;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.correlation.Correlation;
import org.kie.kogito.correlation.SimpleCorrelation;
import org.kie.kogito.event.DataEventFactory;
import org.kie.kogito.event.EventDispatcher;
import org.kie.kogito.event.correlation.DefaultCorrelationService;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.kie.kogito.process.ProcessService;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessEventDispatcherTest {

    public static final String DUMMY_TOPIC = "dummyTopic";
    private Process<DummyModel> process;
    private ProcessService processService;
    private ExecutorService executor;
    private ProcessInstance<DummyModel> processInstance;
    private ProcessInstances<DummyModel> processInstances;
    private DefaultCorrelationService correlationService;

    @BeforeEach
    void setup() {
        process = mock(Process.class);
        processInstances = mock(ProcessInstances.class);
        processInstance = mock(ProcessInstance.class);
        when(processInstance.id()).thenReturn("1");
        when(process.instances()).thenReturn(processInstances);
        correlationService = spy(new DefaultCorrelationService());
        when(process.correlations()).thenReturn(correlationService);
        when(processInstances.findById(Mockito.anyString())).thenReturn(Optional.empty());
        when(processInstances.findById("1")).thenReturn(Optional.of(processInstance));
        processService = mock(ProcessService.class);
        when(processService.createProcessInstance(eq(process), any(), any(), any(), any(), any(), any())).thenReturn(processInstance);
        when(processService.signalProcessInstance(eq(process), any(), any(), any())).thenReturn(Optional.of(mock(DummyModel.class)));
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void close() {
        executor.shutdown();
        correlationService.clear();
    }

    private <T> Optional<Function<T, DummyModel>> modelConverter() {
        return Optional.of(DummyModel::new);
    }

    @Test
    void testSigCloudEvent() throws Exception {
        EventDispatcher<Void, TestEvent> dispatcher = new ProcessEventDispatcher<>(process, Optional.empty(), processService, executor, null, o -> o.getData());
        dispatcher.dispatch(DUMMY_TOPIC, new TestCloudEvent(new TestEvent("pepe"), DUMMY_TOPIC, "source", "1")).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> processInstanceId = ArgumentCaptor.forClass(String.class);

        verify(processService, times(1)).signalProcessInstance(Mockito.any(Process.class), processInstanceId.capture(), Mockito.any(Object.class), signal.capture());

        assertThat(signal.getValue()).isEqualTo("Message-" + DUMMY_TOPIC);
        assertThat(processInstanceId.getValue()).isEqualTo("1");
    }

    @Test
    void testCloudEventNewInstanceWithoutReference() throws Exception {
        EventDispatcher<Void, TestEvent> dispatcher = new ProcessEventDispatcher<>(process, modelConverter(), processService, executor, null, o -> o.getData());
        dispatcher.dispatch(DUMMY_TOPIC, new TestCloudEvent(new TestEvent("pepe"), DUMMY_TOPIC)).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);

        verify(processInstances, never()).findById(any());
        verify(processService, never()).signalProcessInstance(eq(process), any(), any(), signal.capture());
        verify(processService, times(1)).signalProcess(eq(process), any(TestEvent.class), signal.capture());

        assertThat(signal.getValue()).isEqualTo(DUMMY_TOPIC);
    }

    @Test
    void testCloudEventNewInstanceWithReference() throws Exception {
        EventDispatcher<Void, TestEvent> dispatcher = new ProcessEventDispatcher<>(process, modelConverter(), processService, executor, null, o -> o.getData());
        dispatcher.dispatch(DUMMY_TOPIC, new TestCloudEvent(new TestEvent("pepe"), DUMMY_TOPIC, "source", "invalidReference")).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);

        verify(processInstances, times(1)).findById("invalidReference");
        verify(processService, never()).signalProcessInstance(eq(process), any(), any(), signal.capture());
        verify(processService, times(1)).signalProcess(eq(process), any(TestEvent.class), signal.capture());

        assertThat(signal.getValue()).isEqualTo(DUMMY_TOPIC);
    }

    @Test
    void testDataEvent() throws Exception {
        EventDispatcher<Void, TestEvent> dispatcher = new ProcessEventDispatcher<>(process, modelConverter(), processService, executor, null, o -> o.getData());
        dispatcher.dispatch(DUMMY_TOPIC, DataEventFactory.from(new TestEvent("pepe"))).toCompletableFuture().get();
        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        verify(processService, times(1)).signalProcess(eq(process), any(TestEvent.class), signal.capture());
        assertEquals(DUMMY_TOPIC, signal.getValue());
    }

    @Test
    void testIgnoredDataEvent() throws Exception {
        EventDispatcher<Void, Object> dispatcher = new ProcessEventDispatcher<>(process, Optional.empty(), processService, executor, null, o -> o.getData());
        final String payload = "{ a = b }";
        dispatcher.dispatch(DUMMY_TOPIC, DataEventFactory.from(payload)).toCompletableFuture().get();
        verify(processService, times(0)).createProcessInstance(eq(process), any(), any(DummyModel.class), any(), any(), isNull(), isNull());
    }

    @Test
    void testStringSigCloudEvent() throws Exception {
        EventDispatcher<Void, String> dispatcher = new ProcessEventDispatcher<>(process, Optional.empty(), processService, executor, null, o -> o.getData());
        dispatcher.dispatch(DUMMY_TOPIC, new TestCloudEvent("pepe", DUMMY_TOPIC, "source", "1")).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> processInstanceId = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> signalObject = ArgumentCaptor.forClass(Object.class);

        verify(processService, times(1)).signalProcessInstance(Mockito.any(Process.class), processInstanceId.capture(), signalObject.capture(), signal.capture());

        assertThat(signal.getValue()).isEqualTo("Message-" + DUMMY_TOPIC);
        assertThat(signalObject.getValue()).isEqualTo("pepe");
        assertThat(processInstanceId.getValue()).isEqualTo("1");
    }

    @Test
    void testIgnoredCloudEvent() throws Exception {
        EventDispatcher<Void, TestEvent> dispatcher = new ProcessEventDispatcher<>(process, modelConverter(), processService, executor, null, o -> o.getData());
        assertNull(dispatcher.dispatch(DUMMY_TOPIC, new TestCloudEvent(new TestEvent("test"), "differentTopic", "differentSource")).toCompletableFuture().get());
    }

    @Test
    void testCloudEventNewInstanceWithCorrelation() throws Exception {
        String userId = "userId";
        String name = "name";
        String userValue = UUID.randomUUID().toString();
        String nameValue = UUID.randomUUID().toString();

        EventDispatcher<Void, TestEvent> dispatcher = new ProcessEventDispatcher<>(process, modelConverter(), processService, executor, Set.of(userId, name), o -> o.getData());
        TestCloudEvent<TestEvent> event = new TestCloudEvent<>(new TestEvent("pepe"), DUMMY_TOPIC, "source");
        event.addExtensionAttribute(userId, userValue);
        event.addExtensionAttribute(name, nameValue);

        dispatcher.dispatch(DUMMY_TOPIC, event).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        assertThat(correlationService.findByCorrelatedId(processInstance.id())).isEmpty();
        verify(processService, times(1)).signalProcess(eq(process), any(TestEvent.class), signal.capture());

        assertThat(signal.getValue()).isEqualTo(DUMMY_TOPIC);
    }

    @Test
    void testSigCloudEventWithCorrelation() throws Exception {
        String userId = "userId";
        String name = "name";
        String userValue = "aaaa";
        String nameValue = "zzzz";
        SimpleCorrelation<String> userCorrelation = new SimpleCorrelation<>(userId, userValue);
        SimpleCorrelation<String> nameCorrelation = new SimpleCorrelation<>(name, nameValue);
        Set<Correlation<?>> correlations = Set.of(userCorrelation, nameCorrelation);
        CompositeCorrelation compositeCorrelation = new CompositeCorrelation(correlations);
        correlationService.create(compositeCorrelation, "1");

        EventDispatcher<Void, TestEvent> dispatcher =
                new ProcessEventDispatcher<>(process, modelConverter(), processService, executor,
                        Set.of(name, userId),
                        o -> o.getData());
        TestCloudEvent<TestEvent> event = new TestCloudEvent<>(new TestEvent("pepe"), DUMMY_TOPIC, "source");
        event.addExtensionAttribute(userId, userValue);
        event.addExtensionAttribute(name, nameValue);
        dispatcher.dispatch(DUMMY_TOPIC, event).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> processInstanceId = ArgumentCaptor.forClass(String.class);

        verify(correlationService).find(compositeCorrelation);
        verify(processService).signalProcessInstance(Mockito.any(Process.class), processInstanceId.capture(), Mockito.any(Object.class), signal.capture());

        assertThat(signal.getValue()).isEqualTo("Message-" + DUMMY_TOPIC);
        assertThat(processInstanceId.getValue()).isEqualTo("1");
    }
}

/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.event.EventConsumer;
import org.kie.kogito.event.EventMarshaller;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstances;
import org.kie.kogito.process.ProcessService;
import org.kie.kogito.services.event.ProcessDataEvent;
import org.kie.kogito.services.event.impl.ProcessEventDispatcher;
import org.kie.kogito.services.event.impl.StringEventMarshaller;
import org.kie.kogito.services.uow.CollectingUnitOfWorkFactory;
import org.kie.kogito.services.uow.DefaultUnitOfWorkManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class EventImplTest {

    private static class DummyEvent {

        private String dummyField;

        @SuppressWarnings("unused")
        public DummyEvent() {
        }

        public DummyEvent(String dummyField) {
            this.dummyField = dummyField;
        }

        @SuppressWarnings("unused")
        public String getDummyField() {
            return dummyField;
        }
    }

    private static class DummyModel implements Model {
        private Object dummyEvent;

        @Override
        public DummyModel fromMap(Map<String, Object> params) {
            this.dummyEvent = params.get("dummyEvent");
            return this;
        }

        @Override
        public Map<String, Object> toMap() {
            return Collections.singletonMap("dummyEvent", dummyEvent);
        }

        public DummyModel(Object dummyEvent) {
            this.dummyEvent = dummyEvent;
        }

        @Override
        public void update(Map<String, Object> params) {
            fromMap(params);
        }
    }

    private static class DummyCloudEvent extends ProcessDataEvent<DummyEvent> {

        public DummyCloudEvent() {
        }

        public DummyCloudEvent(DummyEvent dummyEvent) {
            this(dummyEvent, null);
        }

        public DummyCloudEvent(DummyEvent dummyEvent, String referenceId) {
            super("dummyTopic", dummyEvent, "1", "1", "1", "1", "1", "1", null);
            super.kogitoReferenceId = referenceId;
        }
    }

    private static EventMarshaller<String> marshaller;

    @BeforeAll
    static void init() {
        marshaller = new StringEventMarshaller(new ObjectMapper());
    }

    private Process<DummyModel> process;

    private Application application;
    private ProcessService processService;
    private ExecutorService executor;

    private ProcessInstance<DummyModel> processInstance;
    private ProcessInstances<DummyModel> processInstances;

    @BeforeEach
    void setup() {

        application = mock(Application.class);
        when(application.unitOfWorkManager())
                .thenReturn(new DefaultUnitOfWorkManager(new CollectingUnitOfWorkFactory()));

        process = mock(Process.class);
        processInstances = mock(ProcessInstances.class);
        processInstance = mock(ProcessInstance.class);

        when(process.instances()).thenReturn(processInstances);
        when(processInstances.findById(Mockito.anyString())).thenReturn(Optional.of(processInstance));
        when(process.createInstance(Mockito.any(DummyModel.class))).thenReturn(processInstance);
        processService = mock(ProcessService.class);
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void close() {
        executor.shutdown();
    }

    private <T> Optional<Function<T, DummyModel>> getConvertedMethod() {
        return Optional.of(DummyModel::new);
    }

    @Test
    void testSigCloudEvent() throws Exception {
        EventConsumer<DummyModel> consumer = new ProcessEventDispatcher<>(process, getConvertedMethod().get(), processService, executor);
        final String trigger = "dummyTopic";
        consumer.dispatch(trigger, new DummyCloudEvent(new DummyEvent("pepe"), "1")).toCompletableFuture().get();

        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> processInstanceId = ArgumentCaptor.forClass(String.class);
        verify(processService, times(1)).signalProcessInstance(Mockito.any(Process.class), processInstanceId.capture(), Mockito.any(Object.class), signal.capture());
        assertEquals("Message-" + trigger, signal.getValue());
        //assertEquals("1", processInstanceId.getValue());
    }

    @Test
    void testCloudEvent() throws Exception {
        EventConsumer<DummyModel> consumer = new ProcessEventDispatcher<>(process, getConvertedMethod().get(), processService, executor);

        final String trigger = "dummyTopic";
        consumer.dispatch(trigger, new DummyCloudEvent(new DummyEvent("pepe"))).toCompletableFuture().get();
        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> processInstanceId = ArgumentCaptor.forClass(String.class);
        verify(processService, timeout(1500L).times(1)).createProcessInstance(Mockito.any(Process.class), Mockito.isNull(), Mockito.any(DummyModel.class), Mockito.isNull(), signal.capture(),
                processInstanceId.capture());
        assertEquals(trigger, signal.getValue());
        //assertEquals("1", processInstanceId.getValue());
    }

    @Test
    void testDataEvent() throws Exception {
        EventConsumer<DummyModel> consumer = new ProcessEventDispatcher<>(process, getConvertedMethod().get(), processService, executor);
        final String trigger = "dummyTopic";
        consumer.dispatch(trigger, new DummyEvent("pepe")).toCompletableFuture().get();
        ArgumentCaptor<String> signal = ArgumentCaptor.forClass(String.class);
        verify(processService, timeout(1500L).times(1)).createProcessInstance(Mockito.any(Process.class), Mockito.isNull(), Mockito.any(DummyModel.class), Mockito.isNull(), signal.capture(),
                Mockito.isNull());
        assertEquals(trigger, signal.getValue());
    }

    @Test
    void testDataMarshaller() {
        DummyEvent dataEvent = new DummyEvent("pepe");
        assertEquals(
                "{\"dummyField\":\"pepe\"}",
                marshaller.marshall(dataEvent));
    }

    @Test
    void testEventMarshaller() {
        DummyEvent dataEvent = new DummyEvent("pepe");
        String jsonString = marshaller.marshall(dataEvent);
        assertTrue(jsonString.contains("\"dummyField\":\"pepe\""));
    }

    @Test
    void testEventPayloadException() throws Exception {
        EventConsumer<DummyModel> consumer = new ProcessEventDispatcher<>(process, getConvertedMethod().get(), processService, executor);
        final String trigger = "dummyTopic";
        final String payload = "{ a = b }";
        consumer.dispatch(trigger, payload).toCompletableFuture().get();
        //todo verify
    }
}

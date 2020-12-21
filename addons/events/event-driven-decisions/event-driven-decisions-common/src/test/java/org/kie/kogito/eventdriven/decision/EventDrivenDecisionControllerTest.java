/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.eventdriven.decision;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.Application;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.decision.DecisionTestUtils;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.kogito.decision.DecisionTestUtils.DECISION_SERVICE_NODE_NAME;
import static org.kie.kogito.decision.DecisionTestUtils.MODEL_NAME;
import static org.kie.kogito.decision.DecisionTestUtils.MODEL_NAMESPACE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventDrivenDecisionControllerTest {

    private static final String CLOUDEVENT_OK_DATA_PLACEHOLDER = "%%DATA_EVENT%%";
    private static final String CLOUDEVENT_OK_ID = "a89b61a2-5644-487a-8a86-144855c5dce8";
    private static final String CLOUDEVENT_OK_SOURCE = "SomeEventSource";
    private static final String CLOUDEVENT_OK_TEMPLATE = "" +
            "{\n" +
            "    \"specversion\": \"1.0\",\n" +
            "    \"id\": \"" + CLOUDEVENT_OK_ID + "\",\n" +
            "    \"source\": \"" + CLOUDEVENT_OK_SOURCE + "\",\n" +
            "    \"type\": \"org.kie.kogito.eventdriven.decision.DecisionRequestEvent\",\n" +
            "    \"data\": " + CLOUDEVENT_OK_DATA_PLACEHOLDER + "\n" +
            "}";

    private static final String CLOUDEVENT_IGNORED = "" +
            "{\n" +
            "    \"specversion\": \"1.0\",\n" +
            "    \"id\": \"55c5dce8-5644-487a-8a86-1448a89b61a2\",\n" +
            "    \"source\": \"SomeOtherEventSource\",\n" +
            "    \"type\": \"com.example.SomeCloudEvent\",\n" +
            "    \"data\": {}\n" +
            "}";

    private static final List<String> DATA_EVENT_BAD_REQUEST = Stream.of(
            "{}",
            "{\"modelName\": \"aName\"}",
            "{\"modelNamespace\": \"aNamespace\"}",
            "{\"inputContext\": {}}",
            "{\"modelName\": \"aName\",\"modelNamespace\": \"aNamespace\"}",
            "{\"modelName\": \"aName\",\"inputContext\": {}}",
            "{\"modelNamespace\": \"aNamespace\",\"inputContext\": {}}"
    ).collect(Collectors.toList());

    private static final String DATA_EVENT_NOT_FOUND = "{\"modelName\": \"aName\",\"modelNamespace\": \"aNamespace\",\"inputContext\": {}}";

    private static final String DATA_EVENT_OK_EVALUATE_ALL = "" +
            "{\n" +
            "    \"modelName\": \"Traffic Violation\",\n" +
            "    \"modelNamespace\": \"https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF\",\n" +
            "    \"inputContext\": {\n" +
            "        \"Driver\": {\n" +
            "            \"Age\": 25,\n" +
            "            \"Points\": 13\n" +
            "        },\n" +
            "        \"Violation\": {\n" +
            "            \"Type\": \"speed\",\n" +
            "            \"Actual Speed\": 115,\n" +
            "            \"Speed Limit\": 100\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private static final String DATA_EVENT_OK_EVALUATE_DECISION_SERVICE = "" +
            "{\n" +
            "    \"modelName\": \"Traffic Violation\",\n" +
            "    \"modelNamespace\": \"https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF\",\n" +
            "    \"decisionServiceName\": \"" + DECISION_SERVICE_NODE_NAME + "\",\n" +
            "    \"inputContext\": {\n" +
            "        \"Driver\": {\n" +
            "            \"Age\": 25,\n" +
            "            \"Points\": 13\n" +
            "        },\n" +
            "        \"Violation\": {\n" +
            "            \"Type\": \"speed\",\n" +
            "            \"Actual Speed\": 115,\n" +
            "            \"Speed Limit\": 100\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private static final String TEST_EXECUTION_ID = "11ecbb6f-fb25-4597-88c8-ac7976efe078";

    private static DMNRuntime runtime;

    private EventDrivenDecisionController controller;
    private CloudEventEmitter eventEmitterMock;
    private DecisionModel decisionModelSpy;
    private DecisionModels decisionModelsMock;

    @BeforeAll
    static void beforeAll() {
        runtime = DecisionTestUtils.createDMNRuntime();
    }

    @BeforeEach
    void beforeEach() {
        decisionModelsMock = mock(DecisionModels.class);
        eventEmitterMock = mock(CloudEventEmitter.class);

        Application applicationMock = mock(Application.class);
        when(applicationMock.get(eq(DecisionModels.class))).thenReturn(decisionModelsMock);

        // by default there's no execution id supplier, if needed it will be overridden in the specific test
        mockDecisionModel();

        controller = new EventDrivenDecisionController(applicationMock, mock(ConfigBean.class), eventEmitterMock, mock(CloudEventReceiver.class));
    }

    @Test
    void testSubscribe() {
        Application applicationMock = mock(Application.class);
        ConfigBean configMock = mock(ConfigBean.class);
        CloudEventEmitter eventEmitterMock = mock(CloudEventEmitter.class);
        CloudEventReceiver eventReceiverMock = mock(CloudEventReceiver.class);

        // option #1: parameters via constructor + parameterless setup
        EventDrivenDecisionController controller1 = new EventDrivenDecisionController(applicationMock, configMock, eventEmitterMock, eventReceiverMock);
        controller1.setup();
        verify(eventReceiverMock).subscribe(any());

        reset(eventReceiverMock);

        // option #2: parameterless via constructor + parameters via setup (introduced for Quarkus CDI)
        EventDrivenDecisionController controller2 = new EventDrivenDecisionController();
        controller2.setup(applicationMock, configMock, eventEmitterMock, eventReceiverMock);
        verify(eventReceiverMock).subscribe(any());
    }

    @Test
    void testHandleEventWithMalformedInput() {
        controller.handleEvent("this-is-not-a-cloudevent");
        verify(eventEmitterMock, never()).emit(any());
    }

    @Test
    void testHandleEventWithIgnoredCloudEvent() {
        controller.handleEvent(CLOUDEVENT_IGNORED);
        verify(eventEmitterMock, never()).emit(any());
    }

    @Test
    void testHandleEventWithValidCloudEventWithNullRequest() {
        controller.handleEvent(cloudEventOkWith("null"));
        verify(eventEmitterMock, never()).emit(any());
    }

    @Test
    void testHandleEventWithValidCloudEventProducingOkEvaluateAll() {
        testCloudEventEmitted(DATA_EVENT_OK_EVALUATE_ALL, DecisionResponseStatus.OK, (cloudEvent, responseEvent) -> {
            assertNull(responseEvent.getErrorMessage());
            assertNull(responseEvent.getExecutionId());
            verify(decisionModelSpy).evaluateAll(notNull());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
        });
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingOkEvaluateAll() {
        mockDecisionModelWithExecutionIdSupplier();
        testCloudEventEmitted(DATA_EVENT_OK_EVALUATE_ALL, DecisionResponseStatus.OK, (cloudEvent, responseEvent) -> {
            assertNull(responseEvent.getErrorMessage());
            assertEquals(TEST_EXECUTION_ID, responseEvent.getExecutionId());
            verify(decisionModelSpy).evaluateAll(notNull());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
        });
    }

    @Test
    void testHandleEventWithValidCloudEventProducingOkEvaluateDecisionService() {
        testCloudEventEmitted(DATA_EVENT_OK_EVALUATE_DECISION_SERVICE, DecisionResponseStatus.OK, (cloudEvent, responseEvent) -> {
            assertNull(responseEvent.getErrorMessage());
            assertNull(responseEvent.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy).evaluateDecisionService(notNull(), notNull());
        });
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingOkEvaluateDecisionService() {
        mockDecisionModelWithExecutionIdSupplier();
        testCloudEventEmitted(DATA_EVENT_OK_EVALUATE_DECISION_SERVICE, DecisionResponseStatus.OK, (cloudEvent, responseEvent) -> {
            assertNull(responseEvent.getErrorMessage());
            assertEquals(TEST_EXECUTION_ID, responseEvent.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy).evaluateDecisionService(notNull(), notNull());
        });
    }

    @Test
    void testHandleEventWithValidCloudEventProducingBadRequest() {
        for (String badRequestData : DATA_EVENT_BAD_REQUEST) {
            testCloudEventEmitted(badRequestData, DecisionResponseStatus.BAD_REQUEST, (cloudEvent, responseEvent) -> {
                assertNotNull(responseEvent.getErrorMessage());
                assertNull(responseEvent.getExecutionId());
                verify(decisionModelSpy, never()).evaluateAll(any());
                verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
            });
        }
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingBadRequest() {
        mockDecisionModelWithExecutionIdSupplier();
        for (String badRequestData : DATA_EVENT_BAD_REQUEST) {
            testCloudEventEmitted(badRequestData, DecisionResponseStatus.BAD_REQUEST, (cloudEvent, responseEvent) -> {
                assertNotNull(responseEvent.getErrorMessage());
                assertNull(responseEvent.getExecutionId());
                verify(decisionModelSpy, never()).evaluateAll(any());
                verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
            });
        }
    }

    @Test
    void testHandleEventWithValidCloudEventProducingNotFound() {
        testCloudEventEmitted(DATA_EVENT_NOT_FOUND, DecisionResponseStatus.NOT_FOUND, (cloudEvent, responseEvent) -> {
            assertNotNull(responseEvent.getErrorMessage());
            assertNull(responseEvent.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
        });
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingNotFound() {
        mockDecisionModelWithExecutionIdSupplier();
        testCloudEventEmitted(DATA_EVENT_NOT_FOUND, DecisionResponseStatus.NOT_FOUND, (cloudEvent, responseEvent) -> {
            assertNotNull(responseEvent.getErrorMessage());
            assertNull(responseEvent.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
        });
    }

    private void assertSubject(CloudEvent event) {
        assertNotNull(event.getSubject());
        assertTrue(event.getSubject().contains("\"id\":\"" + CLOUDEVENT_OK_ID + "\""));
        assertTrue(event.getSubject().contains("\"source\":\"" + CLOUDEVENT_OK_SOURCE + "\""));
    }

    private String cloudEventOkWith(String data) {
        return CLOUDEVENT_OK_TEMPLATE.replace(CLOUDEVENT_OK_DATA_PLACEHOLDER, data);
    }

    private void mockDecisionModel() {
        decisionModelSpy = spy(new DmnDecisionModel(runtime, MODEL_NAMESPACE, MODEL_NAME));
        when(decisionModelsMock.getDecisionModel(eq(MODEL_NAMESPACE), eq(DecisionTestUtils.MODEL_NAME))).thenReturn(decisionModelSpy);
    }

    private void mockDecisionModelWithExecutionIdSupplier() {
        decisionModelSpy = spy(new DmnDecisionModel(runtime, MODEL_NAMESPACE, MODEL_NAME, () -> TEST_EXECUTION_ID));
        when(decisionModelsMock.getDecisionModel(eq(MODEL_NAMESPACE), eq(DecisionTestUtils.MODEL_NAME))).thenReturn(decisionModelSpy);
    }

    private void testCloudEventEmitted(String data, DecisionResponseStatus status, BiConsumer<CloudEvent, DecisionResponseEvent> callback) {
        try {
            ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);
            controller.handleEvent(cloudEventOkWith(data));
            verify(eventEmitterMock).emit(eventCaptor.capture());

            String emittedCloudEventJson = eventCaptor.getValue();

            Optional<CloudEvent> optEmittedCloudEvent = CloudEventUtils.decode(emittedCloudEventJson);
            if (optEmittedCloudEvent.isPresent()) {
                CloudEvent emittedCloudEvent = optEmittedCloudEvent.get();
                Optional<DecisionResponseEvent> optResponseEvent = CloudEventUtils.decodeData(emittedCloudEvent, DecisionResponseEvent.class);
                if (optResponseEvent.isPresent()) {
                    DecisionResponseEvent responseEvent = optResponseEvent.get();

                    assertSame(status, responseEvent.getStatus());
                    assertSubject(emittedCloudEvent);

                    if (callback != null) {
                        callback.accept(emittedCloudEvent, responseEvent);
                    }
                } else {
                    fail("Can't decode emitted CloudEvent data of: " + emittedCloudEventJson);
                }
            } else {
                fail("Can't decode emitted CloudEvent");
            }
        } finally {
            reset(eventEmitterMock);
        }
    }
}

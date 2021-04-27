/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.eventdriven.decision;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.cloudevents.extension.KogitoExtension;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.decision.DecisionTestUtils;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;
import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.JsonNode;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.ExtensionProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.kie.kogito.decision.DecisionTestUtils.DECISION_SERVICE_NODE_NAME;
import static org.kie.kogito.decision.DecisionTestUtils.MODEL_NAME;
import static org.kie.kogito.decision.DecisionTestUtils.MODEL_NAMESPACE;
import static org.kie.kogito.eventdriven.decision.EventDrivenDecisionController.REQUEST_EVENT_TYPE;
import static org.kie.kogito.eventdriven.decision.EventDrivenDecisionController.RESPONSE_ERROR_EVENT_TYPE;
import static org.kie.kogito.eventdriven.decision.EventDrivenDecisionController.RESPONSE_EVENT_TYPE;
import static org.kie.kogito.eventdriven.decision.EventDrivenDecisionController.RESPONSE_FULL_EVENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventDrivenDecisionControllerTest {

    private static final String MODEL_NAME_PLACEHOLDER = "%%MODEL_NAME%%";
    private static final String MODEL_NAMESPACE_PLACEHOLDER = "%%MODEL_NAMESPACE%%";
    private static final String EVALUATE_DECISION_PLACEHOLDER = "%%EVALUATE_DECISION%%";
    private static final String FULL_RESULT_PLACEHOLDER = "%%FULL_RESULT%%";
    private static final String FILTERED_CTX_PLACEHOLDER = "%%FILTERED_CTX%%";
    private static final String DATA_PLACEHOLDER = "%%DATA%%";

    private static final String CLOUDEVENT_ID = "a89b61a2-5644-487a-8a86-144855c5dce8";
    private static final String CLOUDEVENT_SOURCE = "SomeEventSource";
    private static final String CLOUDEVENT_SUBJECT = "TheSubject";
    private static final String CLOUDEVENT_TEMPLATE = "" +
            "{\n" +
            "    \"specversion\": \"1.0\",\n" +
            "    \"id\": \"" + CLOUDEVENT_ID + "\",\n" +
            "    \"source\": \"" + CLOUDEVENT_SOURCE + "\",\n" +
            "    \"type\": \"" + REQUEST_EVENT_TYPE + "\",\n" +
            "    \"subject\": \"" + CLOUDEVENT_SUBJECT + "\",\n" +
            "    \"" + KogitoExtension.KOGITO_DMN_MODEL_NAME + "\": " + MODEL_NAME_PLACEHOLDER + ",\n" +
            "    \"" + KogitoExtension.KOGITO_DMN_MODEL_NAMESPACE + "\": " + MODEL_NAMESPACE_PLACEHOLDER + ",\n" +
            "    \"" + KogitoExtension.KOGITO_DMN_EVALUATE_DECISION + "\": " + EVALUATE_DECISION_PLACEHOLDER + ",\n" +
            "    \"" + KogitoExtension.KOGITO_DMN_FULL_RESULT + "\": " + FULL_RESULT_PLACEHOLDER + ",\n" +
            "    \"" + KogitoExtension.KOGITO_DMN_FILTERED_CTX + "\": " + FILTERED_CTX_PLACEHOLDER + ",\n" +
            "    \"data\": " + DATA_PLACEHOLDER + "\n" +
            "}";

    private static final String CLOUDEVENT_IGNORED = "" +
            "{\n" +
            "    \"specversion\": \"1.0\",\n" +
            "    \"id\": \"55c5dce8-5644-487a-8a86-1448a89b61a2\",\n" +
            "    \"source\": \"SomeOtherEventSource\",\n" +
            "    \"type\": \"SomeType\",\n" +
            "    \"data\": {}\n" +
            "}";

    private static final List<RequestData> REQUEST_DATA_BAD_REQUEST = Stream.of(
            new RequestData(null, null, null, null),
            new RequestData("aName", null, null, null),
            new RequestData(null, "aNamespace", null, null),
            new RequestData(null, null, null, "{}"),
            new RequestData("aName", "aNamespace", null, null),
            new RequestData("aName", null, null, "{}"),
            new RequestData(null, "aNamespace", null, "{}")).collect(Collectors.toList());

    private static final RequestData REQUEST_DATA_MODEL_NOT_FOUND = new RequestData("aName", "aNamespace", null, "{}");

    private static final RequestData REQUEST_DATA_NULL_CONTEXT = new RequestData(MODEL_NAME, MODEL_NAMESPACE, null, null);

    private static final RequestData REQUEST_DATA_EVALUATE_ALL = new RequestData(MODEL_NAME, MODEL_NAMESPACE, null, "" +
            "{\n" +
            "    \"Driver\": {\n" +
            "        \"Age\": 25,\n" +
            "        \"Points\": 13\n" +
            "    },\n" +
            "    \"Violation\": {\n" +
            "        \"Type\": \"speed\",\n" +
            "        \"Actual Speed\": 115,\n" +
            "        \"Speed Limit\": 100\n" +
            "    }\n" +
            "}");

    private static final RequestData REQUEST_DATA_EVALUATE_DECISION_SERVICE = new RequestData(MODEL_NAME, MODEL_NAMESPACE, DECISION_SERVICE_NODE_NAME, "" +
            "{\n" +
            "    \"Violation\": {\n" +
            "        \"Type\": \"speed\",\n" +
            "        \"Actual Speed\": 115,\n" +
            "        \"Speed Limit\": 100\n" +
            "    }\n" +
            "}");

    private static final String TEST_EXECUTION_ID = "11ecbb6f-fb25-4597-88c8-ac7976efe078";

    private static DMNRuntime runtime;

    private EventDrivenDecisionController controller;
    private CloudEventEmitter eventEmitterMock;
    private DecisionModel decisionModelSpy;
    private DecisionModels decisionModelsMock;

    @BeforeAll
    static void beforeAll() {
        ExtensionProvider.getInstance().registerExtension(KogitoExtension.class, KogitoExtension::new);
        runtime = DecisionTestUtils.createDMNRuntime();
    }

    @BeforeEach
    void beforeEach() {
        decisionModelsMock = mock(DecisionModels.class);
        eventEmitterMock = mock(CloudEventEmitter.class);

        // by default there's no execution id supplier, if needed it will be overridden in the specific test
        mockDecisionModel();

        controller = new EventDrivenDecisionController(decisionModelsMock, mock(ConfigBean.class), eventEmitterMock, mock(CloudEventReceiver.class));
    }

    @Test
    void testSubscribe() {
        DecisionModels decisionModelsMock = mock(DecisionModels.class);
        ConfigBean configMock = mock(ConfigBean.class);
        CloudEventEmitter eventEmitterMock = mock(CloudEventEmitter.class);
        CloudEventReceiver eventReceiverMock = mock(CloudEventReceiver.class);

        // option #1: parameters via constructor + parameterless setup
        EventDrivenDecisionController controller1 = new EventDrivenDecisionController(decisionModelsMock, configMock, eventEmitterMock, eventReceiverMock);
        controller1.setup();
        verify(eventReceiverMock).subscribe(any());

        reset(eventReceiverMock);

        // option #2: parameterless via constructor + parameters via setup (introduced for Quarkus CDI)
        EventDrivenDecisionController controller2 = new EventDrivenDecisionController();
        controller2.setup(decisionModelsMock, configMock, eventEmitterMock, eventReceiverMock);
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
    void testHandleEventWithValidCloudEventWithNullDataProducingBadRequest() {
        testAllErrorCloudEventEmittedCombinations(REQUEST_DATA_NULL_CONTEXT, DecisionResponseError.BAD_REQUEST);
    }

    @Test
    void testHandleEventWithValidCloudEventProducingOkEvaluateAll() {
        testAllDefaultAndFullCloudEventEmittedCombinations(REQUEST_DATA_EVALUATE_ALL, (cloudEvent, kogitoExtension, responseEvent) -> {
            assertNull(kogitoExtension.getExecutionId());
            verify(decisionModelSpy).evaluateAll(notNull());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
            clearInvocations(decisionModelSpy);
        });
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingOkEvaluateAll() {
        mockDecisionModelWithExecutionIdSupplier();
        testAllDefaultAndFullCloudEventEmittedCombinations(REQUEST_DATA_EVALUATE_ALL, (cloudEvent, kogitoExtension, responseEvent) -> {
            assertEquals(TEST_EXECUTION_ID, kogitoExtension.getExecutionId());
            verify(decisionModelSpy).evaluateAll(notNull());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
            clearInvocations(decisionModelSpy);
        });
    }

    @Test
    void testHandleEventWithValidCloudEventProducingOkEvaluateDecisionService() {
        testAllDefaultAndFullCloudEventEmittedCombinations(REQUEST_DATA_EVALUATE_DECISION_SERVICE, (cloudEvent, kogitoExtension, responseEvent) -> {
            assertNull(kogitoExtension.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy).evaluateDecisionService(notNull(), notNull());
            clearInvocations(decisionModelSpy);
        });
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingOkEvaluateDecisionService() {
        mockDecisionModelWithExecutionIdSupplier();
        testAllDefaultAndFullCloudEventEmittedCombinations(REQUEST_DATA_EVALUATE_DECISION_SERVICE, (cloudEvent, kogitoExtension, responseEvent) -> {
            assertEquals(TEST_EXECUTION_ID, kogitoExtension.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy).evaluateDecisionService(notNull(), notNull());
            clearInvocations(decisionModelSpy);
        });
    }

    @Test
    void testHandleEventWithValidCloudEventProducingBadRequest() {
        for (RequestData badRequestData : REQUEST_DATA_BAD_REQUEST) {
            testAllErrorCloudEventEmittedCombinations(badRequestData, DecisionResponseError.BAD_REQUEST);
        }
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingBadRequest() {
        mockDecisionModelWithExecutionIdSupplier();
        for (RequestData badRequestData : REQUEST_DATA_BAD_REQUEST) {
            testAllErrorCloudEventEmittedCombinations(badRequestData, DecisionResponseError.BAD_REQUEST);
        }
    }

    @Test
    void testHandleEventWithValidCloudEventProducingNotFound() {
        testAllErrorCloudEventEmittedCombinations(REQUEST_DATA_MODEL_NOT_FOUND, DecisionResponseError.MODEL_NOT_FOUND);
    }

    @Test
    void testHandleEventWithValidCloudEventWithExecutionIdProducingNotFound() {
        mockDecisionModelWithExecutionIdSupplier();
        testAllErrorCloudEventEmittedCombinations(REQUEST_DATA_MODEL_NOT_FOUND, DecisionResponseError.MODEL_NOT_FOUND);
    }

    private void assertSubject(CloudEvent event) {
        assertNotNull(event.getSubject());
        assertEquals(CLOUDEVENT_SUBJECT, event.getSubject());
    }

    private String cloudEventOkWith(RequestData requestData, Boolean fullResult, Boolean filteredCtx) {
        return CLOUDEVENT_TEMPLATE
                .replace(MODEL_NAME_PLACEHOLDER, format(requestData.getModelName()))
                .replace(MODEL_NAMESPACE_PLACEHOLDER, format(requestData.getModelNamespace()))
                .replace(EVALUATE_DECISION_PLACEHOLDER, format(requestData.getDecision()))
                .replace(FULL_RESULT_PLACEHOLDER, fullResult == null ? "null" : fullResult.toString())
                .replace(FILTERED_CTX_PLACEHOLDER, filteredCtx == null ? "null" : filteredCtx.toString())
                .replace(DATA_PLACEHOLDER, Optional.ofNullable(requestData.getData()).orElse("null"));
    }

    private String format(String input) {
        return Optional.ofNullable(input)
                .map(i -> "\"" + i + "\"")
                .orElse("null");
    }

    private void mockDecisionModel() {
        decisionModelSpy = spy(new DmnDecisionModel(runtime, MODEL_NAMESPACE, MODEL_NAME));
        when(decisionModelsMock.getDecisionModel(eq(MODEL_NAMESPACE), eq(DecisionTestUtils.MODEL_NAME))).thenReturn(decisionModelSpy);
    }

    private void mockDecisionModelWithExecutionIdSupplier() {
        decisionModelSpy = spy(new DmnDecisionModel(runtime, MODEL_NAMESPACE, MODEL_NAME, () -> TEST_EXECUTION_ID));
        when(decisionModelsMock.getDecisionModel(eq(MODEL_NAMESPACE), eq(DecisionTestUtils.MODEL_NAME))).thenReturn(decisionModelSpy);
    }

    private <T> void testCloudEventEmitted(RequestData requestData, Boolean fullResult, Boolean filteredCtx, Class<T> responseDataClass, String expectedType,
            TriConsumer<CloudEvent, KogitoExtension, T> callback) {
        try {
            ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);

            String inputEvent = cloudEventOkWith(requestData, fullResult, filteredCtx);
            controller.handleEvent(inputEvent);

            verify(eventEmitterMock).emit(eventCaptor.capture());
            String emittedCloudEventJson = eventCaptor.getValue();

            Optional<CloudEvent> optEmittedCloudEvent = CloudEventUtils.decode(emittedCloudEventJson);

            if (!optEmittedCloudEvent.isPresent()) {
                fail("Can't decode emitted CloudEvent");
            }

            CloudEvent emittedCloudEvent = optEmittedCloudEvent.get();

            assertEquals(expectedType, emittedCloudEvent.getType());

            KogitoExtension kogitoExtension = ExtensionProvider.getInstance()
                    .parseExtension(KogitoExtension.class, emittedCloudEvent);

            if (kogitoExtension == null) {
                fail("No Kogito extension in emitted CloudEvent: " + emittedCloudEventJson);
            }

            assertEquals(requestData.getModelName(), kogitoExtension.getDmnModelName());
            assertEquals(requestData.getModelNamespace(), kogitoExtension.getDmnModelNamespace());
            assertEquals(requestData.getDecision(), kogitoExtension.getDmnEvaluateDecision());

            Optional<T> optResponseEvent = CloudEventUtils.decodeData(emittedCloudEvent, responseDataClass);

            if (!optResponseEvent.isPresent()) {
                fail("Can't decode emitted CloudEvent data of: " + emittedCloudEventJson);
            }

            assertSubject(emittedCloudEvent);

            if (callback != null) {
                callback.accept(emittedCloudEvent, kogitoExtension, optResponseEvent.get());
            }
        } finally {
            reset(eventEmitterMock);
        }
    }

    private void testDefaultCloudEventEmitted(RequestData requestData, Boolean fullResult, Boolean filteredCtx, TriConsumer<CloudEvent, KogitoExtension, JsonNode> callback) {
        testCloudEventEmitted(requestData, fullResult, filteredCtx, JsonNode.class, RESPONSE_EVENT_TYPE, (cloudEvent, kogitoExtension, data) -> {
            if (data.isObject()) {
                assertFalse(data.hasNonNull("namespace"));
                assertFalse(data.hasNonNull("modelName"));
                assertFalse(data.hasNonNull("dmnContext"));
                assertFalse(data.hasNonNull("messages"));
                assertFalse(data.hasNonNull("decisionResults"));
            }
            callback.accept(cloudEvent, kogitoExtension, data);
        });
    }

    private void testFullCloudEventEmitted(RequestData requestData, Boolean fullResult, Boolean filteredCtx, TriConsumer<CloudEvent, KogitoExtension, JsonNode> callback) {
        testCloudEventEmitted(requestData, fullResult, filteredCtx, JsonNode.class, RESPONSE_FULL_EVENT_TYPE, (cloudEvent, kogitoExtension, data) -> {
            assertTrue(data.isObject());
            assertTrue(data.hasNonNull("namespace"));
            assertTrue(data.hasNonNull("modelName"));
            assertTrue(data.hasNonNull("dmnContext"));
            assertTrue(data.hasNonNull("messages"));
            assertTrue(data.hasNonNull("decisionResults"));
            callback.accept(cloudEvent, kogitoExtension, data);
        });
    }

    private void testAllDefaultAndFullCloudEventEmittedCombinations(RequestData requestData, TriConsumer<CloudEvent, KogitoExtension, JsonNode> consumer) {
        testDefaultCloudEventEmitted(requestData, null, null, consumer);
        testDefaultCloudEventEmitted(requestData, null, false, consumer);
        testDefaultCloudEventEmitted(requestData, null, true, consumer);
        testDefaultCloudEventEmitted(requestData, false, null, consumer);
        testDefaultCloudEventEmitted(requestData, false, false, consumer);
        testDefaultCloudEventEmitted(requestData, false, true, consumer);
        testFullCloudEventEmitted(requestData, true, null, consumer);
        testFullCloudEventEmitted(requestData, true, false, consumer);
        testFullCloudEventEmitted(requestData, true, true, consumer);
    }

    private void testErrorCloudEventEmitted(RequestData requestData, Boolean fullResult, Boolean filteredCtx, DecisionResponseError expectedError) {
        testCloudEventEmitted(requestData, fullResult, filteredCtx, DecisionResponseError.class, RESPONSE_ERROR_EVENT_TYPE, (cloudEvent, kogitoExtension, data) -> {
            assertSame(expectedError, data);
            assertNull(kogitoExtension.getExecutionId());
            verify(decisionModelSpy, never()).evaluateAll(any());
            verify(decisionModelSpy, never()).evaluateDecisionService(any(), any());
        });
    }

    private void testAllErrorCloudEventEmittedCombinations(RequestData requestData, DecisionResponseError expectedError) {
        testErrorCloudEventEmitted(requestData, null, null, expectedError);
        testErrorCloudEventEmitted(requestData, null, false, expectedError);
        testErrorCloudEventEmitted(requestData, null, true, expectedError);
        testErrorCloudEventEmitted(requestData, false, null, expectedError);
        testErrorCloudEventEmitted(requestData, false, false, expectedError);
        testErrorCloudEventEmitted(requestData, false, true, expectedError);
        testErrorCloudEventEmitted(requestData, true, null, expectedError);
        testErrorCloudEventEmitted(requestData, true, false, expectedError);
        testErrorCloudEventEmitted(requestData, true, true, expectedError);
    }

    @FunctionalInterface
    private interface TriConsumer<T, U, V> {

        void accept(T t, U u, V v);
    }

    private static class RequestData {

        private final String modelName;
        private final String modelNamespace;
        private final String decision;
        private final String data;

        public RequestData(String modelName, String modelNamespace, String decision, String data) {
            this.modelName = modelName;
            this.modelNamespace = modelNamespace;
            this.decision = decision;
            this.data = data;
        }

        public String getModelName() {
            return modelName;
        }

        public String getModelNamespace() {
            return modelNamespace;
        }

        public String getDecision() {
            return decision;
        }

        public String getData() {
            return data;
        }
    }
}

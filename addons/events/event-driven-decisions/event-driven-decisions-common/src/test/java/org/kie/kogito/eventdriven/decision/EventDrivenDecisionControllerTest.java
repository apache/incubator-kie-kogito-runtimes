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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.Application;
import org.kie.kogito.conf.ConfigBean;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.kogito.decision.DecisionTestUtils;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.event.CloudEventEmitter;
import org.kie.kogito.event.CloudEventReceiver;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertTrue;
import static org.kie.kogito.decision.DecisionTestUtils.MODEL_NAME;
import static org.kie.kogito.decision.DecisionTestUtils.MODEL_NAMESPACE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventDrivenDecisionControllerTest {

    private static DMNRuntime runtime;

    private EventDrivenDecisionController controller;
    private Application applicationMock;
    private ConfigBean configMock;
    private CloudEventEmitter eventEmitterMock;
    private CloudEventReceiver eventReceiverMock;
    private DecisionModel decisionModel;
    private DecisionModels decisionModelsMock;

    @BeforeAll
    static void beforeAll() {
        runtime = DecisionTestUtils.createDMNRuntime();
    }

    @BeforeEach
    void beforeEach() {
        decisionModel = new DmnDecisionModel(runtime, MODEL_NAMESPACE, MODEL_NAME);

        decisionModelsMock = mock(DecisionModels.class);
        when(decisionModelsMock.getDecisionModel(eq(MODEL_NAMESPACE), eq(DecisionTestUtils.MODEL_NAME))).thenReturn(decisionModel);

        applicationMock = mock(Application.class);
        when(applicationMock.get(eq(DecisionModels.class))).thenReturn(decisionModelsMock);

        configMock = mock(ConfigBean.class);
        eventEmitterMock = mock(CloudEventEmitter.class);
        eventReceiverMock = mock(CloudEventReceiver.class);

        controller = new EventDrivenDecisionController(applicationMock, configMock, eventEmitterMock, eventReceiverMock);
    }

    @Test
    void testHandleEventWithMalformedInput() {
        controller.handleEvent("this-is-not-a-cloudevent");
        verify(eventEmitterMock, never()).emit(any());
    }

    @Test
    void testHandleEventWithIgnoredCloudEventType() {
        controller.handleEvent(CLOUDEVENT_IGNORED);
        verify(eventEmitterMock, never()).emit(any());
    }

    @Test
    void testHandleEventWithValidCloudEventTypeWithNullRequest() {
        controller.handleEvent(cloudEventOkWith("null"));
        verify(eventEmitterMock, never()).emit(any());
    }

    @Test
    void testHandleEventWithValidCloudEventTypeProducingOk() {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);

        controller.handleEvent(cloudEventOkWith(DATA_EVENT_OK));
        verify(eventEmitterMock).emit(eventCaptor.capture());

        assertTrue(eventCaptor.getValue().contains("\"status\":\"OK\""));
    }

    @Test
    void testHandleEventWithValidCloudEventTypeProducingBadRequest() {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);

        List<String> badRequestDataList = Stream.of(
               "{}",
                "{\"modelName\": \"aName\"}",
                "{\"modelNamespace\": \"aNamespace\"}",
                "{\"inputContext\": {}}",
                "{\"modelName\": \"aName\",\"modelNamespace\": \"aNamespace\"}",
                "{\"modelName\": \"aName\",\"inputContext\": {}}",
                "{\"modelNamespace\": \"aNamespace\",\"inputContext\": {}}"
        ).collect(Collectors.toList());

        for(String badRequestData : badRequestDataList) {
            controller.handleEvent(cloudEventOkWith(badRequestData));
            verify(eventEmitterMock).emit(eventCaptor.capture());

            assertTrue(eventCaptor.getValue().contains("\"status\":\"BAD_REQUEST\""));

            reset(eventEmitterMock);
        }
    }

    @Test
    void testHandleEventWithValidCloudEventTypeProducingNotFound() {
        ArgumentCaptor<String> eventCaptor = ArgumentCaptor.forClass(String.class);

        controller.handleEvent(cloudEventOkWith("{\"modelName\": \"aName\",\"modelNamespace\": \"aNamespace\",\"inputContext\": {}}"));
        verify(eventEmitterMock).emit(eventCaptor.capture());

        assertTrue(eventCaptor.getValue().contains("\"status\":\"NOT_FOUND\""));
    }

    private static String cloudEventOkWith(String data) {
        return CLOUDEVENT_OK_TEMPLATE.replace("%%DATA_EVENT%%", data);
    }

    private static final String CLOUDEVENT_IGNORED = "" +
            "{\n" +
            "    \"specversion\": \"1.0\",\n" +
            "    \"id\": \"55c5dce8-5644-487a-8a86-1448a89b61a2\",\n" +
            "    \"source\": \"SomeEventSource\",\n" +
            "    \"type\": \"com.example.SomeCloudEvent\",\n" +
            "    \"data\": {}\n" +
            "}";

    private static final String CLOUDEVENT_OK_TEMPLATE = "" +
            "{\n" +
            "    \"specversion\": \"1.0\",\n" +
            "    \"id\": \"a89b61a2-5644-487a-8a86-144855c5dce8\",\n" +
            "    \"source\": \"LoanEligibility\",\n" +
            "    \"type\": \"org.kie.kogito.eventdriven.decision.DecisionRequestEvent\",\n" +
            "    \"data\": %%DATA_EVENT%%\n" +
            "}";

    private static final String DATA_EVENT_OK = "" +
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

}

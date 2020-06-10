/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.event.AfterEvaluateAllEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateAllEvent;
import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.DMNResultImpl;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEvent;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEventType;
import org.kie.kogito.tracing.decision.mock.MockAfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.mock.MockBeforeEvaluateAllEvent;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.tracing.decision.mock.MockUtils.TEST_MODEL_NAME;
import static org.kie.kogito.tracing.decision.mock.MockUtils.TEST_MODEL_NAMESPACE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DecisionTracingListenerTest {

    private static final String REAL_DECISION_SERVICE_NODE_ID = "_073E3815-F30F-4835-A5CF-A9B354444E09";
    private static final String REAL_DECISION_SERVICE_NODE_NAME = "FineService";
    private static final String REAL_MODEL_RESOURCE = "/Traffic Violation.dmn";
    private static final String REAL_MODEL_NAMESPACE = "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF";
    private static final String REAL_MODEL_NAME = "Traffic Violation";

    private static final String TEST_EXECUTION_ID_1 = "e3140fbb-49fd-4835-bb2e-682bbe02d862";
    private static final String TEST_EXECUTION_ID_2 = "77408667-f218-40b0-a355-1bab047a3e9e";

    @Test
    public void test_Listener_MockedEvents_Working() {
        DMNContextImpl context = new DMNContextImpl();
        DecisionExecutionIdUtils.inject(context, () -> TEST_EXECUTION_ID_1);

        DMNResultImpl result = new DMNResultImpl(new DMNModelImpl());
        result.setContext(context);

        BeforeEvaluateAllEvent beforeEvent = new MockBeforeEvaluateAllEvent(TEST_MODEL_NAMESPACE, TEST_MODEL_NAME, result);
        AfterEvaluateAllEvent afterEvent = new MockAfterEvaluateAllEvent(TEST_MODEL_NAMESPACE, TEST_MODEL_NAME, result);

        Consumer<EvaluateEvent> eventConsumer = mock(Consumer.class);
        DecisionTracingListener listener = new DecisionTracingListener(eventConsumer);
        listener.beforeEvaluateAll(beforeEvent);
        listener.afterEvaluateAll(afterEvent);

        ArgumentCaptor<EvaluateEvent> eventCaptor = ArgumentCaptor.forClass(EvaluateEvent.class);
        verify(eventConsumer, times(2)).accept(eventCaptor.capture());

        assertEvaluateAllEvents(eventCaptor.getAllValues(), TEST_MODEL_NAMESPACE, TEST_MODEL_NAME, TEST_EXECUTION_ID_1);
    }

    @Test
    public void test_Listener_RealEvaluateAll_Working() {
        final Map<String, Object> driver = new HashMap<>();
        driver.put("Age", 25);
        driver.put("Points", 10);
        final Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 115);
        violation.put("Speed Limit", 100);
        final Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Driver", driver);
        contextVariables.put("Violation", violation);

        testWithRealEvaluateAll(contextVariables, 14);
    }

    @Test
    public void test_Listener_RealEvaluateAllWithWarnMessage_Working() {
        final Map<String, Object> driver = new HashMap<>();
        driver.put("Age", 25);
        driver.put("Points", 10);
        final Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 105);
        violation.put("Speed Limit", 100);
        final Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Driver", driver);
        contextVariables.put("Violation", violation);

        testWithRealEvaluateAll(contextVariables, 14);
    }

    @Test
    public void test_Listener_RealEvaluateAllWithErrorMessage_Working() {
        final Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 105);
        violation.put("Speed Limit", 100);
        final Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Violation", violation);

        testWithRealEvaluateAll(contextVariables, 10);
    }

    @Test
    public void test_Listener_RealEvaluateDecisionService_Working() {
        final Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 115);
        violation.put("Speed Limit", 100);
        final Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Violation", violation);

        testWithRealEvaluateDecisionService(contextVariables, 6);
    }

    @Test
    public void test_Listener_RealEvaluateDecisionServiceWithWarnMessage_Working() {
        final Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 105);
        violation.put("Speed Limit", 100);
        final Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Violation", violation);

        testWithRealEvaluateDecisionService(contextVariables, 6);
    }

    @Test
    public void test_Listener_RealEvaluateDecisionServiceWithEmptyContext_Working() {
        final Map<String, Object> contextVariables = new HashMap<>();
        testWithRealEvaluateDecisionService(contextVariables, 6);
    }

    private static void testWithRealEvaluateAll(Map<String, Object> contextVariables, int expectedEvents) {
        List<EvaluateEvent> events = testWithRealRuntime(contextVariables, expectedEvents, DecisionModel::evaluateAll);
        assertEvaluateAllEvents(events, REAL_MODEL_NAMESPACE, REAL_MODEL_NAME, TEST_EXECUTION_ID_2);
    }

    private static void testWithRealEvaluateDecisionService(Map<String, Object> contextVariables, int expectedEvents) {
        List<EvaluateEvent> events = testWithRealRuntime(contextVariables, expectedEvents, (model, context) -> model.evaluateDecisionService(context, REAL_DECISION_SERVICE_NODE_NAME));
        assertEvaluateDecisionServiceEvents(events, REAL_MODEL_NAMESPACE, REAL_MODEL_NAME, TEST_EXECUTION_ID_2);
    }

    private static List<EvaluateEvent> testWithRealRuntime(Map<String, Object> contextVariables, int expectedEvents, BiConsumer<DecisionModel, DMNContext> modelConsumer) {
        final DMNRuntime runtime = DMNKogito.createGenericDMNRuntime(new java.io.InputStreamReader(
                DecisionTracingListenerTest.class.getResourceAsStream(REAL_MODEL_RESOURCE)
        ));

        Consumer<EvaluateEvent> eventConsumer = mock(Consumer.class);
        DecisionTracingListener listener = new DecisionTracingListener(eventConsumer);
        runtime.addListener(listener);

        final DecisionModel model = new DmnDecisionModel(runtime, REAL_MODEL_NAMESPACE, REAL_MODEL_NAME, () -> TEST_EXECUTION_ID_2);
        final DMNContext context = model.newContext(contextVariables);
        modelConsumer.accept(model, context);

        ArgumentCaptor<EvaluateEvent> eventCaptor = ArgumentCaptor.forClass(EvaluateEvent.class);
        verify(eventConsumer, times(expectedEvents)).accept(eventCaptor.capture());

        return eventCaptor.getAllValues();
    }

    private static void assertEvaluateAllEvents(List<EvaluateEvent> evaluateEvents, String modelNamespace, String modelName, String executionId) {
        assertTrue(evaluateEvents.size() >= 2);

        evaluateEvents.forEach(e -> assertEventMatches(modelNamespace, modelName, executionId, e));

        EvaluateEvent beforeEvent = evaluateEvents.get(0);
        assertSame(EvaluateEventType.BEFORE_EVALUATE_ALL, beforeEvent.getType());

        EvaluateEvent afterEvent = evaluateEvents.get(evaluateEvents.size() - 1);
        assertSame(EvaluateEventType.AFTER_EVALUATE_ALL, afterEvent.getType());
    }

    private static void assertEvaluateDecisionServiceEvents(List<EvaluateEvent> evaluateEvents, String modelNamespace, String modelName, String executionId) {
        assertTrue(evaluateEvents.size() >= 2);

        evaluateEvents.forEach(e -> assertEventMatches(modelNamespace, modelName, executionId, e));

        EvaluateEvent beforeEvent = evaluateEvents.get(0);
        assertSame(EvaluateEventType.BEFORE_EVALUATE_DECISION_SERVICE, beforeEvent.getType());
        assertEquals(REAL_DECISION_SERVICE_NODE_ID, beforeEvent.getNodeId());
        assertEquals(REAL_DECISION_SERVICE_NODE_NAME, beforeEvent.getNodeName());

        EvaluateEvent afterEvent = evaluateEvents.get(evaluateEvents.size() - 1);
        assertSame(EvaluateEventType.AFTER_EVALUATE_DECISION_SERVICE, afterEvent.getType());
        assertEquals(REAL_DECISION_SERVICE_NODE_ID, afterEvent.getNodeId());
        assertEquals(REAL_DECISION_SERVICE_NODE_NAME, afterEvent.getNodeName());
    }

    private static void assertEventMatches(String modelNamespace, String modelName, String executionId, EvaluateEvent event) {
        assertTrue(event.getModelNamespace() == null && event.getModelName() == null || event.getModelNamespace() != null && event.getModelName() != null);
        if (event.getModelNamespace() != null) {
            assertEquals(modelNamespace, event.getModelNamespace());
            assertEquals(modelName, event.getModelName());
        }
        assertEquals(executionId, event.getExecutionId());
    }
}

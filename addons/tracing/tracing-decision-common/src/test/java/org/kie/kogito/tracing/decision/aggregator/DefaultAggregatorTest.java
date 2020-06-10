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

package org.kie.kogito.tracing.decision.aggregator;

import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNModel;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.tracing.decision.DecisionTracingListenerTest;
import org.kie.kogito.tracing.decision.event.common.InternalMessageType;
import org.kie.kogito.tracing.decision.event.common.MessageCategory;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEvent;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEventType;
import org.kie.kogito.tracing.decision.event.trace.TraceEvent;
import org.kie.kogito.tracing.decision.event.trace.TraceEventType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultAggregatorTest {

    private static final String EVALUATE_ALL_JSON_RESOURCE = "/Traffic Violation_EvaluateEvents_evaluateAll.json";
    private static final String EVALUATE_DECISION_SERVICE_JSON_RESOURCE = "/Traffic Violation_EvaluateEvents_evaluateDecisionService.json";

    private static final String TEST_EXECUTION_ID = "4ac4c69f-4925-4221-b67e-4b14ce47bef8";
    private static final String FIRST_DECISION_NODE_ID = "_4055D956-1C47-479C-B3F4-BAEB61F1C929";
    private static final String LAST_DECISION_NODE_ID = "_8A408366-D8E9-4626-ABF3-5F69AA01F880";
    private static final TypeReference<List<EvaluateEvent>> EVALUATE_EVENT_LIST_TYPE = new TypeReference<>() {
    };

    private static DMNModel model;

    @BeforeAll
    public static void initModel() {
        final String modelResource = "/Traffic Violation.dmn";
        final String modelNamespace = "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF";
        final String modelName = "Traffic Violation";

        Reader modelReader = new java.io.InputStreamReader(DecisionTracingListenerTest.class.getResourceAsStream(modelResource));
        model = DMNKogito.createGenericDMNRuntime(modelReader).getModel(modelNamespace, modelName);
    }

    @Test
    public void test_Aggregate_NullList_NotEnoughData() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, null);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNotEnoughData(traceEvent);
    }

    @Test
    public void test_Aggregate_EmptyList_NotEnoughData() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, Collections.emptyList());
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNotEnoughData(traceEvent);
    }

    @Test
    public void test_Aggregate_EvaluateAll_ValidList_Working() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE);
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEvent(traceEvent, 2, 2, 2);
    }

    @Test
    public void test_Aggregate_EvaluateAll_NullModel_DmnModelNotFound() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE);
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(null, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEvent(traceEvent, 2, 2, 2);
        assertTraceEventInternalMessage(traceEvent, InternalMessageType.DMN_MODEL_NOT_FOUND);
    }

    @Test
    public void test_Aggregate_EvaluateAll_ListWithOnlyFirstEvent_NoExecutionSteps() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE).stream()
                .limit(1).collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEvent(traceEvent, 2, 2, 0);
    }

    @Test
    public void test_Aggregate_EvaluateAll_ListWithMissingFirstBeforeEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE).stream()
                .filter(e -> !(e.getType() == EvaluateEventType.BEFORE_EVALUATE_DECISION && FIRST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 2, 2, 6);
    }

    @Test
    public void test_Aggregate_EvaluateAll_ListWithMissingFirstAfterEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE).stream()
                .filter(e -> !(e.getType() == EvaluateEventType.AFTER_EVALUATE_DECISION && FIRST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 2, 2, 5);
    }

    @Test
    public void test_Aggregate_EvaluateAll_ListWithMissingLastBeforeEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE).stream()
                .filter(e -> !(e.getType() == EvaluateEventType.BEFORE_EVALUATE_DECISION && LAST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 2, 2, 6);
    }

    @Test
    public void test_Aggregate_EvaluateAll_ListWithMissingLastAfterEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_ALL_JSON_RESOURCE).stream()
                .filter(e -> !(e.getType() == EvaluateEventType.AFTER_EVALUATE_DECISION && LAST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 2, 2, 5);
    }

    @Test
    public void test_Aggregate_EvaluateDecisionService_ValidList_Working() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_DECISION_SERVICE_JSON_RESOURCE);
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEvent(traceEvent, 1, 1, 1);
    }

    @Test
    public void test_Aggregate_EvaluateDecisionService_NullModel_DmnModelNotFound() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_DECISION_SERVICE_JSON_RESOURCE);
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(null, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEvent(traceEvent, 1, 1, 1);
        assertTraceEventInternalMessage(traceEvent, InternalMessageType.DMN_MODEL_NOT_FOUND);
    }

    @Test
    public void test_Aggregate_EvaluateDecisionService_ListWithOnlyFirstEvent_NoExecutionSteps() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_DECISION_SERVICE_JSON_RESOURCE).stream()
                .limit(1).collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEvent(traceEvent, 1, 0, 0);
    }

    @Test
    public void test_Aggregate_EvaluateDecisionService_ListWithMissingBeforeEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_DECISION_SERVICE_JSON_RESOURCE).stream()
                .filter(e -> !(e.getType() == EvaluateEventType.BEFORE_EVALUATE_DECISION && FIRST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 1, 1, 3);
    }

    @Test
    public void test_Aggregate_EvaluateDecisionService_ListWithMissingAfterEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource(EVALUATE_DECISION_SERVICE_JSON_RESOURCE).stream()
                .filter(e -> !(e.getType() == EvaluateEventType.AFTER_EVALUATE_DECISION && FIRST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 1, 1, 2);
    }

    private static TraceEvent assertValidCloudEventAndGetData(CloudEventImpl<TraceEvent> cloudEvent) {
        assertEquals(TEST_EXECUTION_ID, cloudEvent.getAttributes().getId());
        assertEquals(TraceEvent.class.getName(), cloudEvent.getAttributes().getType());
        assertTrue(cloudEvent.getData().isPresent());
        return cloudEvent.getData().get();
    }

    private static void assertTraceEventWithNoExecutionStepsHierarchy(TraceEvent traceEvent, int inputsSize, int outputsSize, int executionStepsSize) {
        assertTraceEvent(traceEvent, inputsSize, outputsSize, executionStepsSize);
        assertTraceEventInternalMessage(traceEvent, InternalMessageType.NO_EXECUTION_STEP_HIERARCHY);
    }

    private static void assertTraceEventWithNotEnoughData(TraceEvent traceEvent) {
        assertTraceEvent(traceEvent, 0, 0, 0);
        assertTraceEventInternalMessage(traceEvent, InternalMessageType.NOT_ENOUGH_DATA);
        assertSame(0L, traceEvent.getHeader().getDuration());
    }

    private static void assertTraceEvent(TraceEvent traceEvent, int inputsSize, int outputsSize, int executionStepsSize) {
        assertSame(TraceEventType.DMN, traceEvent.getHeader().getType());
        assertSame(inputsSize, traceEvent.getInputs().size());
        assertSame(outputsSize, traceEvent.getOutputs().size());
        assertSame(executionStepsSize, traceEvent.getExecutionSteps().size());
    }

    private static void assertTraceEventInternalMessage(TraceEvent traceEvent, InternalMessageType type) {
        assertTrue(traceEvent.getHeader().getMessages().stream().anyMatch(
                m -> m.getLevel() == type.getLevel() && m.getCategory() == MessageCategory.INTERNAL && type.name().equals(m.getType())
        ));
    }

    private static List<EvaluateEvent> readEvaluateEventsFromJsonResource(String resourceName) {
        return Json.fromInputStream(
                DefaultAggregatorTest.class.getResourceAsStream(resourceName),
                EVALUATE_EVENT_LIST_TYPE
        );
    }
}

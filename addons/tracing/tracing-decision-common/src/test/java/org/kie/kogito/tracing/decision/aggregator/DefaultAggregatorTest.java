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
import org.kie.api.builder.Message;
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
    public void test_Aggregate_ValidList_Working() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        List<EvaluateEvent> events = readEvaluateEventsFromJsonResource();
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        assertValidCloudEventAndGetData(cloudEvent);
    }

    @Test
    public void test_Aggregate_NullModel_DmnModelNotFound() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        List<EvaluateEvent> events = readEvaluateEventsFromJsonResource();
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(null, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertSame(TraceEventType.DMN, traceEvent.getHeader().getType());
        assertTrue(traceEvent.getHeader().getMessages().stream().anyMatch(
                m -> m.getLevel() == Message.Level.ERROR && m.getCategory() == MessageCategory.INTERNAL && InternalMessageType.DMN_MODEL_NOT_FOUND.name().equals(m.getType())
        ));
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
    public void test_Aggregate_ListWithOnlyFirstEvent_NoExecutionSteps() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource().stream().limit(1).collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertSame(TraceEventType.DMN, traceEvent.getHeader().getType());
        assertTrue(traceEvent.getExecutionSteps().isEmpty());
    }

    @Test
    public void test_Aggregate_ListWithMissingFirstBeforeEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource().stream()
                .filter(e -> !(e.getType() == EvaluateEventType.BEFORE_EVALUATE_DECISION && FIRST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 6);
    }

    @Test
    public void test_Aggregate_ListWithMissingFirstAfterEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource().stream()
                .filter(e -> !(e.getType() == EvaluateEventType.AFTER_EVALUATE_DECISION && FIRST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 5);
    }

    @Test
    public void test_Aggregate_ListWithMissingLastBeforeEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource().stream()
                .filter(e -> !(e.getType() == EvaluateEventType.BEFORE_EVALUATE_DECISION && LAST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 6);
    }

    @Test
    public void test_Aggregate_ListWithMissingLastAfterEvaluateDecisionEvent_NoExecutionStepHierarchy() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = readEvaluateEventsFromJsonResource().stream()
                .filter(e -> !(e.getType() == EvaluateEventType.AFTER_EVALUATE_DECISION && LAST_DECISION_NODE_ID.equals(e.getNodeId())))
                .collect(Collectors.toList());
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(model, TEST_EXECUTION_ID, events);
        TraceEvent traceEvent = assertValidCloudEventAndGetData(cloudEvent);
        assertTraceEventWithNoExecutionStepsHierarchy(traceEvent, 5);
    }

    private static TraceEvent assertValidCloudEventAndGetData(CloudEventImpl<TraceEvent> cloudEvent) {
        assertEquals(TEST_EXECUTION_ID, cloudEvent.getAttributes().getId());
        assertEquals(TraceEvent.class.getName(), cloudEvent.getAttributes().getType());
        assertTrue(cloudEvent.getData().isPresent());
        return cloudEvent.getData().get();
    }

    private static void assertTraceEventWithNoExecutionStepsHierarchy(TraceEvent traceEvent, int expectedExecutionStepsSize) {
        assertSame(TraceEventType.DMN, traceEvent.getHeader().getType());
        assertSame(expectedExecutionStepsSize, traceEvent.getExecutionSteps().size());
        assertTrue(traceEvent.getHeader().getMessages().stream().anyMatch(
                m -> m.getLevel() == Message.Level.WARNING && m.getCategory() == MessageCategory.INTERNAL && InternalMessageType.NO_EXECUTION_STEP_HIERARCHY.name().equals(m.getType())
        ));
    }

    private static void assertTraceEventWithNotEnoughData(TraceEvent traceEvent) {
        assertSame(TraceEventType.DMN, traceEvent.getHeader().getType());
        assertSame(0L, traceEvent.getHeader().getDuration());
        assertTrue(traceEvent.getHeader().getMessages().stream().anyMatch(
                m -> m.getLevel() == Message.Level.ERROR && m.getCategory() == MessageCategory.INTERNAL && InternalMessageType.NOT_ENOUGH_DATA.name().equals(m.getType())
        ));
        assertTrue(traceEvent.getInputs().isEmpty());
        assertTrue(traceEvent.getOutputs().isEmpty());
        assertTrue(traceEvent.getExecutionSteps().isEmpty());
    }

    private static List<EvaluateEvent> readEvaluateEventsFromJsonResource() {
        return Json.fromInputStream(
                DefaultAggregatorTest.class.getResourceAsStream("/Traffic Violation_EvaluateEvents.json"),
                EVALUATE_EVENT_LIST_TYPE
        );
    }
}

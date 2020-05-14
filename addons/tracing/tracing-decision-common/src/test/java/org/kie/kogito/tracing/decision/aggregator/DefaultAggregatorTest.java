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

import java.util.Collections;
import java.util.List;

import io.cloudevents.v1.CloudEventImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNModel;
import org.kie.kogito.tracing.decision.event.evaluate.EvaluateEvent;
import org.kie.kogito.tracing.decision.event.trace.TraceEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.kogito.tracing.decision.mock.MockUtils.afterEvaluateAllEvent;
import static org.kie.kogito.tracing.decision.mock.MockUtils.beforeEvaluateAllEvent;
import static org.kie.kogito.tracing.decision.mock.MockUtils.mockedModel;

public class DefaultAggregatorTest {

    private static final String TEST_EXECUTION_ID = "4ac4c69f-4925-4221-b67e-4b14ce47bef8";

    private static DMNModel mockedModel;

    @BeforeAll
    public static void initMockedModel() {
        mockedModel = mockedModel();
    }

    @Test
    public void test_Aggregate_ValidList_Working() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = List.of(
                beforeEvaluateAllEvent(TEST_EXECUTION_ID),
                afterEvaluateAllEvent(TEST_EXECUTION_ID)
        );
        CloudEventImpl<TraceEvent> cloudEvent = aggregator.aggregate(mockedModel, TEST_EXECUTION_ID, events);
        assertEquals(TEST_EXECUTION_ID, cloudEvent.getAttributes().getId());
        assertEquals(TraceEvent.class.getName(), cloudEvent.getAttributes().getType());
    }

    @Test
    public void test_Aggregate_NullList_ExceptionThrown() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        assertThrows(AggregatorException.class, () -> aggregator.aggregate(mockedModel, TEST_EXECUTION_ID, null));
    }

    @Test
    public void test_Aggregate_EmptyList_ExceptionThrown() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        assertThrows(AggregatorException.class, () -> aggregator.aggregate(mockedModel, TEST_EXECUTION_ID, Collections.emptyList()));
    }

    @Test
    public void test_Aggregate_ListWithoutTraceEvent_ExceptionThrown() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = List.of(
                beforeEvaluateAllEvent(TEST_EXECUTION_ID)
        );
        assertThrows(AggregatorException.class, () -> aggregator.aggregate(mockedModel, TEST_EXECUTION_ID, events));
    }
}

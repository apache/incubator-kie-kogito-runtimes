package org.kie.kogito.tracing.decision.aggregator;

import java.util.Collections;
import java.util.List;

import io.cloudevents.v1.CloudEventImpl;
import org.junit.jupiter.api.Test;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.kogito.tracing.decision.mock.MockUtils.afterEvaluateAllEvent;
import static org.kie.kogito.tracing.decision.mock.MockUtils.beforeEvaluateAllEvent;

public class DefaultAggregatorTest {

    private static final String TEST_EXECUTION_ID = "4ac4c69f-4925-4221-b67e-4b14ce47bef8";

    @Test
    public void test_Aggregate_ValidList_Working() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = List.of(
                beforeEvaluateAllEvent(TEST_EXECUTION_ID),
                afterEvaluateAllEvent(TEST_EXECUTION_ID)
        );
        CloudEventImpl<AfterEvaluateAllEvent> cloudEvent = aggregator.aggregate(TEST_EXECUTION_ID, events);
        assertEquals(TEST_EXECUTION_ID, cloudEvent.getAttributes().getId());
        assertEquals(AfterEvaluateAllEvent.class.getName(), cloudEvent.getAttributes().getType());
    }

    @Test
    public void test_Aggregate_NullList_ExceptionThrown() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        assertThrows(IllegalStateException.class, () -> aggregator.aggregate(TEST_EXECUTION_ID, null));
    }

    @Test
    public void test_Aggregate_EmptyList_ExceptionThrown() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        assertThrows(IllegalStateException.class, () -> aggregator.aggregate(TEST_EXECUTION_ID, Collections.emptyList()));
    }

    @Test
    public void test_Aggregate_ListWithoutAfterEvaluateAllEvent_ExceptionThrown() {
        final DefaultAggregator aggregator = new DefaultAggregator();
        final List<EvaluateEvent> events = List.of(
                beforeEvaluateAllEvent(TEST_EXECUTION_ID)
        );
        assertThrows(IllegalStateException.class, () -> aggregator.aggregate(TEST_EXECUTION_ID, events));
    }

}

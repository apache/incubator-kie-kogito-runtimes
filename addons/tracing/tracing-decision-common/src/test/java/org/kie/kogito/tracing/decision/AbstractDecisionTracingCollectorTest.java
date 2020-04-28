package org.kie.kogito.tracing.decision;

import java.util.List;
import java.util.Map;

import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventImpl;
import org.junit.jupiter.api.Test;
import org.kie.dmn.feel.util.Pair;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;
import org.kie.kogito.tracing.decision.mock.MockDecisionTracingCollector;
import org.kie.kogito.tracing.decision.mock.MockDefaultAggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.tracing.decision.mock.MockUtils.afterEvaluateAllEvent;
import static org.kie.kogito.tracing.decision.mock.MockUtils.beforeEvaluateAllEvent;

public class AbstractDecisionTracingCollectorTest {

    private static final String TEST_EXECUTION_ID_1 = "c91da8ec-05f7-4dbd-adf4-c7aa88f7888b";
    private static final String TEST_EXECUTION_ID_2 = "550e2947-0952-4225-81a0-ea6e1064efd2";

    @Test
    public void test_Collector_InterleavedEvaluations_Working() {
        MockDefaultAggregator aggregator = new MockDefaultAggregator();
        MockDecisionTracingCollector collector = new MockDecisionTracingCollector(aggregator);

        collector.handleEvaluateEvent(beforeEvaluateAllEvent(TEST_EXECUTION_ID_1));
        collector.handleEvaluateEvent(beforeEvaluateAllEvent(TEST_EXECUTION_ID_2));
        collector.handleEvaluateEvent(afterEvaluateAllEvent(TEST_EXECUTION_ID_1));
        collector.handleEvaluateEvent(afterEvaluateAllEvent(TEST_EXECUTION_ID_2));

        Map<String, Pair<List<EvaluateEvent>, CloudEventImpl<AfterEvaluateAllEvent>>> aggregatorCalls = aggregator.getCalls();
        assertEquals(2, aggregatorCalls.size());
        assertTrue(aggregatorCalls.containsKey(TEST_EXECUTION_ID_1));
        assertEquals(2, aggregatorCalls.get(TEST_EXECUTION_ID_1).getLeft().size());
        assertTrue(aggregatorCalls.containsKey(TEST_EXECUTION_ID_2));
        assertEquals(2, aggregatorCalls.get(TEST_EXECUTION_ID_2).getLeft().size());

        List<String> payloads = collector.getPayloads();
        assertEquals(2, payloads.size());
        assertEquals(Json.encode(aggregatorCalls.get(TEST_EXECUTION_ID_1).getRight()), payloads.get(0));
        assertEquals(Json.encode(aggregatorCalls.get(TEST_EXECUTION_ID_2).getRight()), payloads.get(1));
    }

}

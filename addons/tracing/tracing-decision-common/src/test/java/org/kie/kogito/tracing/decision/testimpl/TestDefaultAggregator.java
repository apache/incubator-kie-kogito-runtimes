package org.kie.kogito.tracing.decision.testimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.cloudevents.v1.CloudEventImpl;
import org.kie.dmn.feel.util.Pair;
import org.kie.kogito.tracing.decision.aggregator.DefaultAggregator;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

public class TestDefaultAggregator extends DefaultAggregator {

    final Map<String, Pair<List<EvaluateEvent>, CloudEventImpl<AfterEvaluateAllEvent>>> calls = new HashMap<>();

    public Map<String, Pair<List<EvaluateEvent>, CloudEventImpl<AfterEvaluateAllEvent>>> getCalls() {
        return calls;
    }

    @Override
    public CloudEventImpl<AfterEvaluateAllEvent> aggregate(String evaluationId, List<EvaluateEvent> events) {
        CloudEventImpl<AfterEvaluateAllEvent> result = super.aggregate(evaluationId, events);
        calls.put(evaluationId, new Pair<>(events, result));
        return result;
    }

}

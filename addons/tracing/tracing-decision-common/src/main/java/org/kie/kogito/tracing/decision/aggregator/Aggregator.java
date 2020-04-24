package org.kie.kogito.tracing.decision.aggregator;

import java.util.List;

import io.cloudevents.v1.CloudEventImpl;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

@FunctionalInterface
public interface Aggregator<T> {

    CloudEventImpl<T> aggregate(String evaluationId, List<EvaluateEvent> events);

}

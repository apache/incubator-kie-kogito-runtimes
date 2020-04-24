package org.kie.kogito.tracing.decision.aggregator;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

public class DefaultAggregator implements Aggregator<AfterEvaluateAllEvent> {

    @Override
    public CloudEventImpl<AfterEvaluateAllEvent> aggregate(String evaluationId, List<EvaluateEvent> events) {
        AfterEvaluateAllEvent event = Optional.ofNullable(events)
                .filter(l -> !l.isEmpty())
                .map(l -> l.get(l.size() - 1))
                .filter(AfterEvaluateAllEvent.class::isInstance)
                .map(AfterEvaluateAllEvent.class::cast)
                .orElseThrow(() -> new IllegalStateException("Invalid event list"));

        return CloudEventBuilder.<AfterEvaluateAllEvent>builder()
                .withType(AfterEvaluateAllEvent.class.getName())
                .withId(evaluationId)
                .withSource(URI.create(event.getModelName()))
                .withData(event)
                .build();
    }

}

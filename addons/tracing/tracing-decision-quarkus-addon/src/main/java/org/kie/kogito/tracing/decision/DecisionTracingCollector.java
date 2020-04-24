package org.kie.kogito.tracing.decision;

import javax.inject.Singleton;

import io.quarkus.vertx.ConsumeEvent;
import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;
import org.reactivestreams.Publisher;

@Singleton
public class DecisionTracingCollector extends AbstractDecisionTracingCollector {

    private final PublishSubject<String> eventSubject = PublishSubject.create();

    @Outgoing("kogito-tracing-decision")
    public Publisher<String> getEventPublisher() {
        return eventSubject.toFlowable(BackpressureStrategy.BUFFER);
    }

    @ConsumeEvent("kogito-tracing-decision_BeforeEvaluateAllEvent")
    public void onEvent(BeforeEvaluateAllEvent event) {
        handleEvaluateEvent(event);
    }

    @ConsumeEvent("kogito-tracing-decision_AfterEvaluateAllEvent")
    public void onEvent(AfterEvaluateAllEvent event) {
        handleEvaluateEvent(event);
    }

    @Override
    protected void handlePayload(String payload) {
        eventSubject.onNext(payload);
    }

}

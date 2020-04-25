package org.kie.kogito.tracing.decision;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.core.eventbus.EventBus;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

@ApplicationScoped
public final class DecisionTracingListener extends AbstractDecisionTracingListener {

    private final EventBus bus;

    @Inject
    public DecisionTracingListener(EventBus bus) {
        this.bus = bus;
    }

    @Override
    protected void handleEvaluateEvent(EvaluateEvent event) {
        bus.send(String.format("kogito-tracing-decision_%s", event.getClass().getSimpleName()), event);
    }

}

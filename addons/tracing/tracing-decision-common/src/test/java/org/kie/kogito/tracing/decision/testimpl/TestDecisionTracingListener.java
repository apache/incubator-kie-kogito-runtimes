package org.kie.kogito.tracing.decision.testimpl;

import java.util.LinkedList;
import java.util.List;

import org.kie.kogito.tracing.decision.AbstractDecisionTracingListener;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

public class TestDecisionTracingListener extends AbstractDecisionTracingListener {

    private final List<EvaluateEvent> events = new LinkedList<>();

    public List<EvaluateEvent> getEvents() {
        return events;
    }

    @Override
    protected void handleEvaluateEvent(EvaluateEvent event) {
        events.add(event);
    }

}

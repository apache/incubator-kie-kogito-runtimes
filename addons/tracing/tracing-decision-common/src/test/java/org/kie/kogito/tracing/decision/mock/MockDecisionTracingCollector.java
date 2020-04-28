package org.kie.kogito.tracing.decision.mock;

import java.util.LinkedList;
import java.util.List;

import org.kie.kogito.tracing.decision.AbstractDecisionTracingCollector;
import org.kie.kogito.tracing.decision.aggregator.Aggregator;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

public class MockDecisionTracingCollector extends AbstractDecisionTracingCollector {

    private final List<String> payloads = new LinkedList<>();

    public MockDecisionTracingCollector(Aggregator<?> aggregator) {
        super(aggregator);
    }

    public void addEvent(EvaluateEvent event) {
        handleEvaluateEvent(event);
    }

    public List<String> getPayloads() {
        return payloads;
    }

    @Override
    protected void handlePayload(String payload) {
        payloads.add(payload);
    }

}

package org.kie.kogito.tracing.decision;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;

public abstract class AbstractDecisionTracingListener implements DMNRuntimeEventListener {

    @Override
    public void beforeEvaluateAll(org.kie.dmn.api.core.event.BeforeEvaluateAllEvent event) {
        handleEvaluateEvent(new BeforeEvaluateAllEvent(
                extractExecutionId(event.getResult().getContext()),
                event.getModelName(),
                event.getModelNamespace(),
                event.getResult().getContext()
        ));
    }

    @Override
    public void afterEvaluateAll(org.kie.dmn.api.core.event.AfterEvaluateAllEvent event) {
        handleEvaluateEvent(new AfterEvaluateAllEvent(
                extractExecutionId(event.getResult().getContext()),
                event.getModelName(),
                event.getModelNamespace(),
                event.getResult()
        ));
    }

    protected abstract void handleEvaluateEvent(EvaluateEvent event);

    private String extractExecutionId(DMNContext context) {
        return DecisionExecutionIdUtils.get(context);
    }

}

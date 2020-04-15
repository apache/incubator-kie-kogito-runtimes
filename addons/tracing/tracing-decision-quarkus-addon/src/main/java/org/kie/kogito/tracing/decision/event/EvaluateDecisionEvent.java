package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNResult;

public abstract class EvaluateDecisionEvent extends EvaluateEvent {

    private final String decisionId;

    public EvaluateDecisionEvent(String evaluationId, String modelName, String modelNamespace, String decisionId, DMNResult result) {
        super(evaluationId, modelName, modelNamespace, result);
        this.decisionId = decisionId;
    }

    public String getDecisionId() {
        return decisionId;
    }

}

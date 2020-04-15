package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNResult;

public class AfterEvaluateDecisionEvent extends EvaluateDecisionEvent {

    public AfterEvaluateDecisionEvent(String evaluationId, String modelName, String modelNamespace, String decisionId, DMNResult result) {
        super(evaluationId, modelName, modelNamespace, decisionId, result);
    }

}

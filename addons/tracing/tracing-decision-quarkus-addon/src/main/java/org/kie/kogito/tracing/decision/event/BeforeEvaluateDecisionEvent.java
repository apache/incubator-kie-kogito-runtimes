package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNResult;

public class BeforeEvaluateDecisionEvent extends EvaluateDecisionEvent {

    public BeforeEvaluateDecisionEvent(String evaluationId, String modelName, String modelNamespace, String decisionId, DMNResult result) {
        super(evaluationId, modelName, modelNamespace, decisionId, result);
    }

}

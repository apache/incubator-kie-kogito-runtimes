package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNResult;

public class AfterEvaluateDecisionEvent extends EvaluateDecisionEvent {

    public AfterEvaluateDecisionEvent(String executionId, String modelName, String modelNamespace, String decisionId, DMNResult result) {
        super(executionId, modelName, modelNamespace, decisionId, result);
    }

}

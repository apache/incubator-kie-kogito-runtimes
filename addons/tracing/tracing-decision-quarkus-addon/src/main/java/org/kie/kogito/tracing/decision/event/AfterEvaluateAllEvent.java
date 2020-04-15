package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNResult;

public class AfterEvaluateAllEvent extends EvaluateEvent {

    public AfterEvaluateAllEvent(String evaluationId, String modelName, String modelNamespace, DMNResult result) {
        super(evaluationId, modelName, modelNamespace, result);
    }

}

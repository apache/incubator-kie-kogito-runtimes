package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNResult;

public class AfterEvaluateAllEvent extends EvaluateEvent {

    public AfterEvaluateAllEvent(String executionId, String modelName, String modelNamespace, DMNResult result) {
        super(executionId, modelName, modelNamespace, result);
    }

}

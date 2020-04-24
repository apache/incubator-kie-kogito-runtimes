package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNContext;

public class BeforeEvaluateAllEvent extends EvaluateEvent {

    public BeforeEvaluateAllEvent(String executionId, String modelName, String modelNamespace, DMNContext context) {
        super(executionId, modelName, modelNamespace, context);
    }

}

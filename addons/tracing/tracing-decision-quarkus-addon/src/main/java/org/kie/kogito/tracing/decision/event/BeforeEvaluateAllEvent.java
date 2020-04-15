package org.kie.kogito.tracing.decision.event;

import org.kie.dmn.api.core.DMNContext;

public class BeforeEvaluateAllEvent extends EvaluateEvent {

    public BeforeEvaluateAllEvent(String evaluationId, String modelName, String modelNamespace, DMNContext context) {
        super(evaluationId, modelName, modelNamespace, context);
    }

}

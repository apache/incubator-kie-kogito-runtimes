package org.kie.kogito.tracing.decision.testimpl;

import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.event.BeforeEvaluateAllEvent;

public class TestBeforeEvaluateAllEvent implements BeforeEvaluateAllEvent {

    private final String modelNamespace;
    private final String modelName;
    private final DMNResult result;

    public TestBeforeEvaluateAllEvent(String modelNamespace, String modelName, DMNResult result) {
        this.modelNamespace = modelNamespace;
        this.modelName = modelName;
        this.result = result;
    }

    @Override
    public String getModelNamespace() {
        return modelNamespace;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public DMNResult getResult() {
        return result;
    }

}

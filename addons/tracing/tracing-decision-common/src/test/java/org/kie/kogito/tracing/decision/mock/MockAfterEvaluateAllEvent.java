package org.kie.kogito.tracing.decision.mock;

import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.event.AfterEvaluateAllEvent;

public class MockAfterEvaluateAllEvent implements AfterEvaluateAllEvent {

    private final String modelNamespace;
    private final String modelName;
    private final DMNResult result;

    public MockAfterEvaluateAllEvent(String modelNamespace, String modelName, DMNResult result) {
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

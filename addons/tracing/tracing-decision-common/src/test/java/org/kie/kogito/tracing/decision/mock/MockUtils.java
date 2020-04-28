package org.kie.kogito.tracing.decision.mock;

import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.DMNResultImpl;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;

public class MockUtils {

    public static final String TEST_MODEL_NAME = "modelName";
    public static final String TEST_MODEL_NAMESPACE = "modelNamespace";

    public static BeforeEvaluateAllEvent beforeEvaluateAllEvent(String id) {
        DMNContextImpl context = new DMNContextImpl();
        DecisionExecutionIdUtils.inject(context, () -> id);
        return new BeforeEvaluateAllEvent(id, TEST_MODEL_NAME, TEST_MODEL_NAMESPACE, context);
    }

    public static AfterEvaluateAllEvent afterEvaluateAllEvent(String id) {
        DMNContextImpl context = new DMNContextImpl();
        DecisionExecutionIdUtils.inject(context, () -> id);
        DMNResultImpl result = new DMNResultImpl(new DMNModelImpl());
        result.setContext(context);
        return new AfterEvaluateAllEvent(id, TEST_MODEL_NAME, TEST_MODEL_NAMESPACE, result);
    }

}

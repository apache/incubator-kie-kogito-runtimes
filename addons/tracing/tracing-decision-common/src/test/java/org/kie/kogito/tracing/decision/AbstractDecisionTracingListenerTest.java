package org.kie.kogito.tracing.decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.api.core.event.AfterEvaluateAllEvent;
import org.kie.dmn.api.core.event.BeforeEvaluateAllEvent;
import org.kie.dmn.core.impl.DMNContextImpl;
import org.kie.dmn.core.impl.DMNModelImpl;
import org.kie.dmn.core.impl.DMNResultImpl;
import org.kie.kogito.decision.DecisionExecutionIdUtils;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;
import org.kie.kogito.tracing.decision.testimpl.TestAfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.testimpl.TestBeforeEvaluateAllEvent;
import org.kie.kogito.tracing.decision.testimpl.TestDecisionTracingListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kie.kogito.tracing.decision.testimpl.TestUtils.TEST_MODEL_NAME;
import static org.kie.kogito.tracing.decision.testimpl.TestUtils.TEST_MODEL_NAMESPACE;

public class AbstractDecisionTracingListenerTest {

    private static final String TEST_EXECUTION_ID_1 = "e3140fbb-49fd-4835-bb2e-682bbe02d862";
    private static final String TEST_EXECUTION_ID_2 = "77408667-f218-40b0-a355-1bab047a3e9e";

    @Test
    public void test_Listener_UseTestEvents_Working() {
        DMNContextImpl context = new DMNContextImpl();
        DecisionExecutionIdUtils.inject(context, () -> TEST_EXECUTION_ID_1);

        DMNResultImpl result = new DMNResultImpl(new DMNModelImpl());
        result.setContext(context);

        BeforeEvaluateAllEvent beforeEvent = new TestBeforeEvaluateAllEvent(TEST_MODEL_NAMESPACE, TEST_MODEL_NAME, result);
        AfterEvaluateAllEvent afterEvent = new TestAfterEvaluateAllEvent(TEST_MODEL_NAMESPACE, TEST_MODEL_NAME, result);

        TestDecisionTracingListener listener = new TestDecisionTracingListener();
        listener.beforeEvaluateAll(beforeEvent);
        listener.afterEvaluateAll(afterEvent);

        assertEvaluateEvents(listener.getEvents(), TEST_MODEL_NAMESPACE, TEST_MODEL_NAME, TEST_EXECUTION_ID_1);
    }

    @Test
    public void test_Listener_UseRealEvents_Working() {
        final String modelResource = "/Traffic Violation.dmn";
        final String modelNamespace = "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF";
        final String modelName = "Traffic Violation";

        final DMNRuntime runtime = DMNKogito.createGenericDMNRuntime(new java.io.InputStreamReader(
                AbstractDecisionTracingListenerTest.class.getResourceAsStream(modelResource)
        ));

        TestDecisionTracingListener listener = new TestDecisionTracingListener();
        runtime.addListener(listener);

        final Map<String, Object> driver = new HashMap<>();
        driver.put("Points", 10);
        final Map<String, Object> violation = new HashMap<>();
        violation.put("Type", "speed");
        violation.put("Actual Speed", 105);
        violation.put("Speed Limit", 100);
        final Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("Driver", driver);
        contextVariables.put("Violation", violation);

        final DecisionModel model = new DmnDecisionModel(runtime, modelNamespace, modelName, () -> TEST_EXECUTION_ID_2);
        final DMNContext context = model.newContext(contextVariables);
        model.evaluateAll(context);

        assertEvaluateEvents(listener.getEvents(), modelNamespace, modelName, TEST_EXECUTION_ID_2);
    }

    private static void assertEvaluateEvents(List<EvaluateEvent> evaluateEvents, String modelNamespace, String modelName, String executionId) {
        assertEquals(2, evaluateEvents.size());
        assertTrue(evaluateEvents.get(0) instanceof org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent);
        assertTrue(evaluateEvents.get(1) instanceof org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent);

        org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent beforeEvaluateAllEvent =
                (org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent) evaluateEvents.get(0);
        assertEquals(executionId, beforeEvaluateAllEvent.getExecutionId());
        assertEquals(modelName, beforeEvaluateAllEvent.getModelName());
        assertEquals(modelNamespace, beforeEvaluateAllEvent.getModelNamespace());

        org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent afterEvaluateAllEvent =
                (org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent) evaluateEvents.get(1);
        assertEquals(executionId, afterEvaluateAllEvent.getExecutionId());
        assertEquals(modelName, afterEvaluateAllEvent.getModelName());
        assertEquals(modelNamespace, afterEvaluateAllEvent.getModelNamespace());
    }

}

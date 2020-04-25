package org.kie.kogito.tracing.decision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventImpl;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.feel.util.Pair;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;
import org.kie.kogito.tracing.decision.testimpl.TestEventBus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecisionTracingTest {

    private static final String TEST_EXECUTION_ID = "7c50581e-6e5b-407b-91d6-2ffb1d47ebc0";

    @Test
    public void test_ListenerAndCollector_UseRealEvents_Working() {
        final String modelResource = "/Traffic Violation.dmn";
        final String modelNamespace = "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF";
        final String modelName = "Traffic Violation";

        final DMNRuntime runtime = DMNKogito.createGenericDMNRuntime(new java.io.InputStreamReader(
                DecisionTracingTest.class.getResourceAsStream(modelResource)
        ));

        TestEventBus eventBus = new TestEventBus();
        DecisionTracingListener listener = new DecisionTracingListener(eventBus);
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

        final DecisionModel model = new DmnDecisionModel(runtime, modelNamespace, modelName, () -> TEST_EXECUTION_ID);
        final DMNContext context = model.newContext(contextVariables);
        model.evaluateAll(context);

        List<Pair<String, Object>> eventBusCalls = eventBus.getCalls();
        assertEquals(2, eventBusCalls.size());
        assertEquals("kogito-tracing-decision_BeforeEvaluateAllEvent", eventBusCalls.get(0).getLeft());
        assertTrue(eventBusCalls.get(0).getRight() instanceof BeforeEvaluateAllEvent);
        assertEquals("kogito-tracing-decision_AfterEvaluateAllEvent", eventBusCalls.get(1).getLeft());
        assertTrue(eventBusCalls.get(1).getRight() instanceof AfterEvaluateAllEvent);

        BeforeEvaluateAllEvent beforeEvent = (BeforeEvaluateAllEvent) eventBusCalls.get(0).getRight();
        AfterEvaluateAllEvent afterEvent = (AfterEvaluateAllEvent) eventBusCalls.get(1).getRight();

        TestSubscriber<String> subscriber = new TestSubscriber<>();

        DecisionTracingCollector collector = new DecisionTracingCollector();
        collector.getEventPublisher().subscribe(subscriber);
        collector.onEvent(beforeEvent);
        collector.onEvent(afterEvent);

        subscriber.assertValueCount(1);

        CloudEventImpl<JsonNode> cloudEvent = Json.decodeValue(subscriber.values().get(0), CloudEventImpl.class, JsonNode.class);
        assertEquals(TEST_EXECUTION_ID, cloudEvent.getAttributes().getId());
    }

}

/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.tracing.decision;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventImpl;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.dmn.DMNKogito;
import org.kie.kogito.dmn.DmnDecisionModel;
import org.kie.kogito.tracing.decision.event.AfterEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.BeforeEvaluateAllEvent;
import org.kie.kogito.tracing.decision.event.EvaluateEvent;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KogitoDecisionTracingTest {

    private static final String TEST_EXECUTION_ID = "7c50581e-6e5b-407b-91d6-2ffb1d47ebc0";
    private static final String TEST_TOPIC = "test-topic";

    @Test
    public void test_ListenerAndCollector_UseRealEvents_Working() {
        final String modelResource = "/Traffic Violation.dmn";
        final String modelNamespace = "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF";
        final String modelName = "Traffic Violation";

        final DMNRuntime runtime = DMNKogito.createGenericDMNRuntime(new java.io.InputStreamReader(
                KogitoDecisionTracingTest.class.getResourceAsStream(modelResource)
        ));

        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        KogitoDecisionTracingListener listener = new KogitoDecisionTracingListener(eventPublisher);
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

        ArgumentCaptor<EvaluateEvent> eventCaptor = ArgumentCaptor.forClass(EvaluateEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        assertTrue(eventCaptor.getAllValues().get(0) instanceof BeforeEvaluateAllEvent);
        assertTrue(eventCaptor.getAllValues().get(1) instanceof AfterEvaluateAllEvent);

        BeforeEvaluateAllEvent beforeEvent = (BeforeEvaluateAllEvent) eventCaptor.getAllValues().get(0);
        AfterEvaluateAllEvent afterEvent = (AfterEvaluateAllEvent) eventCaptor.getAllValues().get(1);

        KafkaTemplate<String, String> template = mock(KafkaTemplate.class);
        KogitoDecisionTracingCollector collector = new KogitoDecisionTracingCollector(template, TEST_TOPIC);
        collector.onApplicationEvent(beforeEvent);
        collector.onApplicationEvent(afterEvent);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).send(topicCaptor.capture(), payloadCaptor.capture());

        assertEquals(TEST_TOPIC, topicCaptor.getValue());

        CloudEventImpl<JsonNode> cloudEvent = Json.decodeValue(payloadCaptor.getValue(), CloudEventImpl.class, JsonNode.class);
        assertEquals(TEST_EXECUTION_ID, cloudEvent.getAttributes().getId());
    }

}

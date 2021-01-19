/*
 *  Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kie.kogito.cloudevents.extension;

import java.net.URI;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.ExtensionProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.cloudevents.CloudEventUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KogitoExtensionTest {

    private static final String TEST_ID = "test-cloudevent-id";
    private static final String TEST_SOURCE = "http://test-cloudevent-source";
    private static final String TEST_TYPE = "test-cloudevent-type";

    private static final String TEST_EXECUTION_ID = "test-kogito-execution-id";
    private static final String TEST_DMN_MODEL_NAME = "test-kogito-dmn-model-name";
    private static final String TEST_DMN_MODEL_NAMESPACE = "test-kogito-dmn-model-namespace";
    private static final String TEST_DMN_EVALUATE_DECISION = "test-kogito-dmn-eval-decision";

    @BeforeAll
    static void registerExtension() {
        KogitoExtension.register();
    }

    @Test
    void writeExtension() {
        KogitoExtension kogitoExt = new KogitoExtension();
        kogitoExt.setExecutionId(TEST_EXECUTION_ID);
        kogitoExt.setDmnModelName(TEST_DMN_MODEL_NAME);
        kogitoExt.setDmnModelNamespace(TEST_DMN_MODEL_NAMESPACE);
        kogitoExt.setDmnEvaluateDecision(TEST_DMN_EVALUATE_DECISION);

        CloudEvent event = CloudEventBuilder
                .v1()
                .withId(TEST_ID)
                .withSource(URI.create(TEST_SOURCE))
                .withType(TEST_TYPE)
                .withExtension(kogitoExt)
                .build();

        assertEquals(TEST_EXECUTION_ID, event.getExtension(KogitoExtension.KOGITO_EXECUTION_ID));
        assertEquals(TEST_DMN_MODEL_NAME, event.getExtension(KogitoExtension.KOGITO_DMN_MODEL_NAME));
        assertEquals(TEST_DMN_MODEL_NAMESPACE, event.getExtension(KogitoExtension.KOGITO_DMN_MODEL_NAMESPACE));
        assertEquals(TEST_DMN_EVALUATE_DECISION, event.getExtension(KogitoExtension.KOGITO_DMN_EVALUATE_DECISION));
    }

    @Test
    void parseExtension() {
        CloudEvent event = CloudEventBuilder.v1()
                .withId(TEST_ID)
                .withSource(URI.create(TEST_SOURCE))
                .withType(TEST_TYPE)
                .withExtension(KogitoExtension.KOGITO_EXECUTION_ID, TEST_EXECUTION_ID)
                .withExtension(KogitoExtension.KOGITO_DMN_MODEL_NAME, TEST_DMN_MODEL_NAME)
                .withExtension(KogitoExtension.KOGITO_DMN_MODEL_NAMESPACE, TEST_DMN_MODEL_NAMESPACE)
                .withExtension(KogitoExtension.KOGITO_DMN_EVALUATE_DECISION, TEST_DMN_EVALUATE_DECISION)
                .build();

        KogitoExtension kogitoExtension = ExtensionProvider
                .getInstance()
                .parseExtension(KogitoExtension.class, event);

        assertNotNull(kogitoExtension);
        assertEquals(TEST_EXECUTION_ID, kogitoExtension.getExecutionId());
        assertEquals(TEST_DMN_MODEL_NAME, kogitoExtension.getDmnModelName());
        assertEquals(TEST_DMN_MODEL_NAMESPACE, kogitoExtension.getDmnModelNamespace());
        assertEquals(TEST_DMN_EVALUATE_DECISION, kogitoExtension.getDmnEvaluateDecision());
    }

    @Test
    void writeAndParseExtensionWithEncoding() {
        KogitoExtension inputKogitoExt = new KogitoExtension();
        inputKogitoExt.setExecutionId(TEST_EXECUTION_ID);
        inputKogitoExt.setDmnModelName(TEST_DMN_MODEL_NAME);
        inputKogitoExt.setDmnModelNamespace(TEST_DMN_MODEL_NAMESPACE);
        inputKogitoExt.setDmnEvaluateDecision(TEST_DMN_EVALUATE_DECISION);

        CloudEvent inputEvent = CloudEventBuilder
                .v1()
                .withId(TEST_ID)
                .withSource(URI.create(TEST_SOURCE))
                .withType(TEST_TYPE)
                .withExtension(inputKogitoExt)
                .withData("\"TEST_DATA\"".getBytes())
                .build();

        String inputEventJson = CloudEventUtils.encode(inputEvent).orElseThrow(IllegalStateException::new);

        CloudEvent outputEvent = CloudEventUtils.decode(inputEventJson).orElseThrow(IllegalStateException::new);

        KogitoExtension outputKogitoExt = ExtensionProvider
                .getInstance()
                .parseExtension(KogitoExtension.class, outputEvent);

        assertNotNull(outputKogitoExt);
        assertEquals(TEST_EXECUTION_ID, outputKogitoExt.getExecutionId());
        assertEquals(TEST_DMN_MODEL_NAME, outputKogitoExt.getDmnModelName());
        assertEquals(TEST_DMN_MODEL_NAMESPACE, outputKogitoExt.getDmnModelNamespace());
        assertEquals(TEST_DMN_EVALUATE_DECISION, outputKogitoExt.getDmnEvaluateDecision());
    }
}

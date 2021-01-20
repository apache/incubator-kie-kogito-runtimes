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
import static org.junit.jupiter.api.Assertions.assertSame;

public class KogitoExtensionTest {

    private static final String TEST_ID = "test-cloudevent-id";
    private static final String TEST_SOURCE = "http://test-cloudevent-source";
    private static final String TEST_TYPE = "test-cloudevent-type";
    private static final String TEST_DATA = "\"TEST_DATA\"";

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
        assertWriteExtension(null, null, null, false, null, false);
        assertWriteExtension(null, null, null, false, false, false);
        assertWriteExtension(null, null, null, false, true, true);
        assertWriteExtension(null, null, false, false, null, false);
        assertWriteExtension(null, null, false, false, false, false);
        assertWriteExtension(null, null, false, false, true, true);
        assertWriteExtension(null, null, true, true, null, false);
        assertWriteExtension(null, null, true, true, false, false);
        assertWriteExtension(null, null, true, true, true, true);

        assertWriteExtension(null, TEST_EXECUTION_ID, null, false, null, false);
        assertWriteExtension(null, TEST_EXECUTION_ID, null, false, false, false);
        assertWriteExtension(null, TEST_EXECUTION_ID, null, false, true, true);
        assertWriteExtension(null, TEST_EXECUTION_ID, false, false, null, false);
        assertWriteExtension(null, TEST_EXECUTION_ID, false, false, false, false);
        assertWriteExtension(null, TEST_EXECUTION_ID, false, false, true, true);
        assertWriteExtension(null, TEST_EXECUTION_ID, true, true, null, false);
        assertWriteExtension(null, TEST_EXECUTION_ID, true, true, false, false);
        assertWriteExtension(null, TEST_EXECUTION_ID, true, true, true, true);

        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, null, false, null, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, null, false, false, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, null, false, true, true);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, false, false, null, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, false, false, false, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, false, false, true, true);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, true, true, null, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, true, true, false, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, null, true, true, true, true);

        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, null, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, false, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, true, true);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, null, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, false, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, true, true);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, null, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, false, false);
        assertWriteExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, true, true);
    }

    private void assertWriteExtension(
            String dmnEvaluateDecision,
            String executionId,
            Boolean inputDmnFullResult, Boolean outputDmnFullResult,
            Boolean inputDmnFilteredCtx, Boolean outputDmnFilteredCtx
    ) {
        CloudEvent event = cloudEventFromExtensionObject(dmnEvaluateDecision, executionId, inputDmnFullResult, inputDmnFilteredCtx);
        assertCloudEvent(event, dmnEvaluateDecision, executionId, outputDmnFullResult, outputDmnFilteredCtx);
    }

    @Test
    void parseExtension() {
        assertParseExtension(null, null, null, false, null, false);
        assertParseExtension(null, null, null, false, false, false);
        assertParseExtension(null, null, null, false, true, true);
        assertParseExtension(null, null, false, false, null, false);
        assertParseExtension(null, null, false, false, false, false);
        assertParseExtension(null, null, false, false, true, true);
        assertParseExtension(null, null, true, true, null, false);
        assertParseExtension(null, null, true, true, false, false);
        assertParseExtension(null, null, true, true, true, true);

        assertParseExtension(null, TEST_EXECUTION_ID, null, false, null, false);
        assertParseExtension(null, TEST_EXECUTION_ID, null, false, false, false);
        assertParseExtension(null, TEST_EXECUTION_ID, null, false, true, true);
        assertParseExtension(null, TEST_EXECUTION_ID, false, false, null, false);
        assertParseExtension(null, TEST_EXECUTION_ID, false, false, false, false);
        assertParseExtension(null, TEST_EXECUTION_ID, false, false, true, true);
        assertParseExtension(null, TEST_EXECUTION_ID, true, true, null, false);
        assertParseExtension(null, TEST_EXECUTION_ID, true, true, false, false);
        assertParseExtension(null, TEST_EXECUTION_ID, true, true, true, true);

        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, null, false, null, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, null, false, false, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, null, false, true, true);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, false, false, null, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, false, false, false, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, false, false, true, true);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, true, true, null, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, true, true, false, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, null, true, true, true, true);

        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, null, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, false, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, true, true);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, null, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, false, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, true, true);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, null, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, false, false);
        assertParseExtension(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, true, true);
    }

    private void assertParseExtension(
            String dmnEvaluateDecision,
            String executionId,
            Boolean inputDmnFullResult, boolean outputDmnFullResult,
            Boolean inputDmnFilteredCtx, boolean outputDmnFilteredCtx
    ) {
        KogitoExtension extension = extensionObjectFromCloudEvent(dmnEvaluateDecision, executionId, inputDmnFullResult, inputDmnFilteredCtx);
        assertExtension(extension, dmnEvaluateDecision, executionId, outputDmnFullResult, outputDmnFilteredCtx);
    }

    @Test
    void writeAndParseExtensionWithEncoding() {
        assertWriteAndParseExtensionWithEncoding(null, null, null, false, null, false);
        assertWriteAndParseExtensionWithEncoding(null, null, null, false, false, false);
        assertWriteAndParseExtensionWithEncoding(null, null, null, false, true, true);
        assertWriteAndParseExtensionWithEncoding(null, null, false, false, null, false);
        assertWriteAndParseExtensionWithEncoding(null, null, false, false, false, false);
        assertWriteAndParseExtensionWithEncoding(null, null, false, false, true, true);
        assertWriteAndParseExtensionWithEncoding(null, null, true, true, null, false);
        assertWriteAndParseExtensionWithEncoding(null, null, true, true, false, false);
        assertWriteAndParseExtensionWithEncoding(null, null, true, true, true, true);

        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, null, false, null, false);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, null, false, false, false);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, null, false, true, true);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, false, false, null, false);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, false, false, false, false);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, false, false, true, true);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, true, true, null, false);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, true, true, false, false);
        assertWriteAndParseExtensionWithEncoding(null, TEST_EXECUTION_ID, true, true, true, true);

        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, null, false, null, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, null, false, false, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, null, false, true, true);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, false, false, null, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, false, false, false, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, false, false, true, true);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, true, true, null, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, true, true, false, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, null, true, true, true, true);

        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, null, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, false, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, null, false, true, true);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, null, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, false, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, false, false, true, true);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, null, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, false, false);
        assertWriteAndParseExtensionWithEncoding(TEST_DMN_EVALUATE_DECISION, TEST_EXECUTION_ID, true, true, true, true);
    }

    private void assertWriteAndParseExtensionWithEncoding(
            String dmnEvaluateDecision,
            String executionId,
            Boolean inputDmnFullResult, boolean outputDmnFullResult,
            Boolean inputDmnFilteredCtx, boolean outputDmnFilteredCtx
    ) {
        CloudEvent inputEvent = cloudEventFromExtensionObject(dmnEvaluateDecision, executionId, inputDmnFullResult, inputDmnFilteredCtx);
        String inputEventJson = CloudEventUtils.encode(inputEvent).orElseThrow(IllegalStateException::new);
        CloudEvent outputEvent = CloudEventUtils.decode(inputEventJson).orElseThrow(IllegalStateException::new);
        KogitoExtension outputKogitoExt = ExtensionProvider.getInstance().parseExtension(KogitoExtension.class, outputEvent);
        assertExtension(outputKogitoExt, dmnEvaluateDecision, executionId, outputDmnFullResult, outputDmnFilteredCtx);
    }

    private CloudEvent cloudEventFromExtensionObject(String dmnEvaluateDecision, String executionId, Boolean dmnFullResult, Boolean dmnFilteredCtx) {
        KogitoExtension kogitoExt = new KogitoExtension();
        kogitoExt.setDmnModelName(TEST_DMN_MODEL_NAME);
        kogitoExt.setDmnModelNamespace(TEST_DMN_MODEL_NAMESPACE);

        if (dmnEvaluateDecision != null) {
            kogitoExt.setDmnEvaluateDecision(dmnEvaluateDecision);
        }

        if (executionId != null) {
            kogitoExt.setExecutionId(executionId);
        }

        if (dmnFullResult != null) {
            kogitoExt.setDmnFullResult(dmnFullResult);
        }

        if (dmnFilteredCtx != null) {
            kogitoExt.setDmnFilteredCtx(dmnFilteredCtx);
        }

        return CloudEventBuilder
                .v1()
                .withId(TEST_ID)
                .withSource(URI.create(TEST_SOURCE))
                .withType(TEST_TYPE)
                .withData(TEST_DATA.getBytes())
                .withExtension(kogitoExt)
                .build();
    }

    private void assertCloudEvent(CloudEvent event, String dmnEvaluateDecision, String executionId, Boolean dmnFullResult, Boolean dmnFilteredCtx) {
        assertNotNull(event);
        assertEquals(TEST_DMN_MODEL_NAME, event.getExtension(KogitoExtension.KOGITO_DMN_MODEL_NAME));
        assertEquals(TEST_DMN_MODEL_NAMESPACE, event.getExtension(KogitoExtension.KOGITO_DMN_MODEL_NAMESPACE));
        assertEquals(dmnEvaluateDecision, event.getExtension(KogitoExtension.KOGITO_DMN_EVALUATE_DECISION));
        assertEquals(executionId, event.getExtension(KogitoExtension.KOGITO_EXECUTION_ID));
        assertEquals(dmnFullResult, event.getExtension(KogitoExtension.KOGITO_DMN_FULL_RESULT));
        assertEquals(dmnFilteredCtx, event.getExtension(KogitoExtension.KOGITO_DMN_FILTERED_CTX));
    }

    private KogitoExtension extensionObjectFromCloudEvent(String dmnEvaluateDecision, String executionId, Boolean dmnFullResult, Boolean dmnFilteredCtx) {
        CloudEventBuilder builder = CloudEventBuilder.v1()
                .withId(TEST_ID)
                .withSource(URI.create(TEST_SOURCE))
                .withType(TEST_TYPE)
                .withData(TEST_DATA.getBytes())
                .withExtension(KogitoExtension.KOGITO_DMN_MODEL_NAME, TEST_DMN_MODEL_NAME)
                .withExtension(KogitoExtension.KOGITO_DMN_MODEL_NAMESPACE, TEST_DMN_MODEL_NAMESPACE);

        if (dmnEvaluateDecision != null) {
            builder.withExtension(KogitoExtension.KOGITO_DMN_EVALUATE_DECISION, dmnEvaluateDecision);
        }

        if (executionId != null) {
            builder.withExtension(KogitoExtension.KOGITO_EXECUTION_ID, executionId);
        }

        if (dmnFullResult != null) {
            builder.withExtension(KogitoExtension.KOGITO_DMN_FULL_RESULT, dmnFullResult);
        }

        if (dmnFilteredCtx != null) {
            builder.withExtension(KogitoExtension.KOGITO_DMN_FILTERED_CTX, dmnFilteredCtx);
        }

        return ExtensionProvider.getInstance().parseExtension(KogitoExtension.class, builder.build());
    }

    private void assertExtension(KogitoExtension kogitoExtension, String dmnEvaluateDecision, String executionId, boolean dmnFullResult, boolean dmnFilteredCtx) {
        assertNotNull(kogitoExtension);
        assertEquals(TEST_DMN_MODEL_NAME, kogitoExtension.getDmnModelName());
        assertEquals(TEST_DMN_MODEL_NAMESPACE, kogitoExtension.getDmnModelNamespace());
        assertEquals(dmnEvaluateDecision, kogitoExtension.getDmnEvaluateDecision());
        assertEquals(executionId, kogitoExtension.getExecutionId());
        assertSame(dmnFullResult, kogitoExtension.isDmnFullResult());
        assertSame(dmnFilteredCtx, kogitoExtension.isDmnFilteredCtx());
    }
}

/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.integrationtests;

import org.junit.jupiter.api.Test;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.cloudevents.extension.KogitoExtension;
import org.kie.kogito.decision.DecisionTestUtils;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.ExtensionProvider;
import io.quarkus.test.junit.QuarkusTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class QuarkusKogitoExtensionInitializerIT {

    @Test
    public void test() {
        String eventJson = "" +
                "{\n" +
                "  \"specversion\": \"1.0\",\n" +
                "  \"id\": \"SomeEventId\",\n" +
                "  \"source\": \"SomeEventSource\",\n" +
                "  \"type\": \"SomeEventType\",\n" +
                "  \"" + KogitoExtension.KOGITO_DMN_MODEL_NAME + "\": \"" + DecisionTestUtils.MODEL_NAME + "\",\n" +
                "  \"" + KogitoExtension.KOGITO_DMN_MODEL_NAMESPACE + "\": \"" + DecisionTestUtils.MODEL_NAMESPACE + "\",\n" +
                "  \"data\": \"{}\"" +
                "}";

        CloudEvent event = CloudEventUtils.decode(eventJson).orElseThrow(IllegalStateException::new);
        KogitoExtension kogitoExtension = ExtensionProvider.getInstance().parseExtension(KogitoExtension.class, event);

        assertNotNull(kogitoExtension, "KogitoExtension not registered, please make sure " +
                "bean org.kie.kogito.addon.cloudevents.quarkus.QuarkusKogitoExtensionInitializer has been loaded");
        assertEquals(DecisionTestUtils.MODEL_NAME, kogitoExtension.getDmnModelName());
        assertEquals(DecisionTestUtils.MODEL_NAMESPACE, kogitoExtension.getDmnModelNamespace());
    }
}

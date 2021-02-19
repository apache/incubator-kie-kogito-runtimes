/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.events.knative.ce.extensions;

import java.net.URI;
import java.util.UUID;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.ExtensionProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KogitoProcessExtensionTest {

    @BeforeAll
    static void setupTest() {
        ExtensionProvider.getInstance().registerExtension(KogitoProcessExtension.class, KogitoProcessExtension::new);
    }

    @Test
    void verifyKogitoExtensionCanBeRead() {
        final KogitoProcessExtension kpe = new KogitoProcessExtension();
        kpe.readFrom(getExampleCloudEvent());
        assertThat(kpe.getValue(KogitoProcessExtension.REF_ID)).isNotNull().isInstanceOf(String.class).isEqualTo("12345");
        assertThat(kpe.getValue(KogitoProcessExtension.PROCESS_ID)).isNotNull().isInstanceOf(String.class).isEqualTo("super_process");
        assertThat((String)kpe.getValue(KogitoProcessExtension.PROCESS_INSTANCE_ID)).isBlank();
        assertThat((String)kpe.getValue(KogitoProcessExtension.PROCESS_INSTANCE_STATE)).isBlank();
        assertThat((String)kpe.getValue(KogitoProcessExtension.ROOT_PROCESS_INSTANCE_ID)).isBlank();
        assertThat((String)kpe.getValue(KogitoProcessExtension.ROOT_PROCESS_ID)).isBlank();
        assertThat((String)kpe.getValue(KogitoProcessExtension.PARENT_PROCESS_INSTANCE_ID)).isBlank();
        assertThat((String)kpe.getValue(KogitoProcessExtension.ADDONS)).isBlank();
    }

    @Test
    void verifyKeysAreSet() {
        final KogitoProcessExtension kpe = ExtensionProvider.getInstance().parseExtension(KogitoProcessExtension.class, getExampleCloudEvent());
        assertThat(kpe.getKeys()).isNotEmpty();
    }

    private CloudEvent getExampleCloudEvent() {
        return CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("example.demo")
                .withSource(URI.create("http://example.com"))
                .withData("application/json", "{}".getBytes())
                .withExtension(KogitoProcessExtension.REF_ID, "12345")
                .withExtension(KogitoProcessExtension.PROCESS_ID, "super_process")
                .build();
    }
}
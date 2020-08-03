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
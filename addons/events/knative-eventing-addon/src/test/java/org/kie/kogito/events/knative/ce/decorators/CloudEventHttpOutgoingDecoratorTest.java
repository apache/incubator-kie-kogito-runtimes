package org.kie.kogito.events.knative.ce.decorators;

import io.smallrye.reactive.messaging.http.HttpResponseMetadata;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CloudEventHttpOutgoingDecoratorTest {

    @Test
    void verifyDecorateAndSend() {
        final String payload = "any message";
        final Message<String> message = new CloudEventHttpOutgoingDecorator().decorate(payload);
        assertThat(message).isNotNull();
        assertThat(message.getMetadata(HttpResponseMetadata.class)).isPresent();
    }
}
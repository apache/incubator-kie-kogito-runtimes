package org.kie.kogito.events.knative.ce.decorators;

import io.smallrye.reactive.messaging.http.HttpResponseMetadata;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

class CloudEventHttpOutgoingDecoratorTest {

    @Test
    void verifyDecorateAndSend() {
        final String payload = "any message";
        final Message<String> message = new CloudEventHttpOutgoingDecorator().decorate(payload);
        Assertions.assertThat(message).isNotNull();
        Assertions.assertThat(message.getMetadata(HttpResponseMetadata.class)).isPresent();
    }
}
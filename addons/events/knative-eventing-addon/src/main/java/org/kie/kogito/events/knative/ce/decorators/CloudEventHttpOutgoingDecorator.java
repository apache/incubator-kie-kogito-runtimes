package org.kie.kogito.events.knative.ce.decorators;

import io.smallrye.reactive.messaging.http.HttpResponseMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.kie.kogito.events.knative.ce.http.ExtMediaType;

import javax.ws.rs.core.HttpHeaders;
import java.util.Collections;

/**
 * Decorators for Http CloudEvents outgoing messages
 */
public final class CloudEventHttpOutgoingDecorator implements Decorator {

    /**
     * Metadata to include content-type for structured CloudEvents messages
     */
    static final Metadata HTTP_RESPONSE_METADATA =
            Metadata.of(HttpResponseMetadata.builder()
                    .withQueryParameter(Collections.emptyMap())
                    .withHeader(HttpHeaders.CONTENT_TYPE, ExtMediaType.CLOUDEVENTS_JSON).build());

    CloudEventHttpOutgoingDecorator() {

    }

    /**
     * Decorates a given payload with custom metadata needed by Http Outgoing processing
     *
     * @param payload of the given message
     * @param <T>     Payload type
     */
    public <T> Message<T> decorate(T payload) {
        return Message.of(payload, HTTP_RESPONSE_METADATA);
    }
}

package org.kie.kogito.events.knative.ce.decorators;

import org.eclipse.microprofile.reactive.messaging.Message;

/**
 * {@link Decorator}s decorates the {@link Message} envelope with metadata and additional information in a given context.
 */
public interface Decorator {

    /**
     * Decorates the given reactive message
     *
     * @param payload payload to decorate
     * @param <T>     payload type
     * @return payload in Message format decorated
     */
    <T> Message<T> decorate(T payload);

}

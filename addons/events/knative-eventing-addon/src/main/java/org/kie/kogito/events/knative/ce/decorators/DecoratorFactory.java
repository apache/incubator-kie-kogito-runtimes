package org.kie.kogito.events.knative.ce.decorators;

import java.util.Optional;

/**
 * Decorator Factory
 */
public final class DecoratorFactory {

    private static final String SMALLRYE_HTTP_METADATA_CLASS = "io.smallrye.reactive.messaging.http.HttpResponseMetadata";

    private DecoratorFactory() {
    }

    /**
     * Builds a new {@link Decorator} depending on the implementation being presented in the classpath.
     *
     * @return an {@link Optional} instance of {@link Decorator}
     */
    public static Optional<Decorator> newInstance() {
        try {
            Class.forName(SMALLRYE_HTTP_METADATA_CLASS, false, DecoratorFactory.class.getClassLoader());
            return Optional.of(new CloudEventHttpOutgoingDecorator());
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

}

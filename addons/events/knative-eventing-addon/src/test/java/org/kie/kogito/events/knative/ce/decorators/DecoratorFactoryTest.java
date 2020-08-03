package org.kie.kogito.events.knative.ce.decorators;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DecoratorFactoryTest {

    @Test
    void verifyCloudEventHttpIsOnClasspath() {
        final Optional<Decorator> decorator = DecoratorFactory.newInstance();
        assertThat(decorator).isPresent();
        assertThat(decorator.get()).isInstanceOf(CloudEventHttpOutgoingDecorator.class);
    }
}
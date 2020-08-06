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
package org.kie.kogito.events.knative.ce.decorators;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageMessageDecoratorFactoryTest {

    @Test
    void verifyCloudEventHttpIsOnClasspath() {
        final Optional<MessageDecorator> decorator = MessageDecoratorFactory.newInstance();
        assertThat(decorator).isPresent();
        assertThat(decorator.get()).isInstanceOf(CloudEventHttpOutgoingDecorator.class);
    }
}
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
package org.kie.kogito.addon.quarkus.messaging.common.message;

import java.util.Collections;
import java.util.Optional;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;
import org.kie.kogito.KogitoGAV;
import org.kie.kogito.addon.quarkus.common.reactive.messaging.MessageDecoratorProvider;
import org.kie.kogito.config.ConfigBean;

import io.quarkus.reactivemessaging.http.runtime.OutgoingHttpMetadata;
import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class CloudEventHttpOutgoingDecoratorTest {

    @Inject
    MessageDecoratorProvider provider;

    @Produces
    CloudEventHttpOutgoingDecorator decorator = new CloudEventHttpOutgoingDecorator();

    @Produces
    ConfigBean configBean = new ConfigBean() {

        @Override
        public boolean useCloudEvents() {
            return true;
        }

        @Override
        public String getServiceUrl() {
            return null;
        }

        @Override
        public Optional<KogitoGAV> getGav() {
            return Optional.empty();
        }
    };

    @Test
    void verifyOutgoingHttpMetadataIsSet() {
        Message<String> message = provider.decorate(Message.of("pepe"));
        Optional<OutgoingHttpMetadata> metadata = message.getMetadata(OutgoingHttpMetadata.class);
        assertThat(metadata).isNotEmpty();
        assertThat(metadata.orElseThrow().getHeaders()).containsEntry("Content-Type", Collections.singletonList("application/cloudevents+json"));
    }
}

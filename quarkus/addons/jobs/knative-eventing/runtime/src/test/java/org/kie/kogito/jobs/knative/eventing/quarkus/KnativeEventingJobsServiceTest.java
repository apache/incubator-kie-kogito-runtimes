/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jobs.knative.eventing.quarkus;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.kie.kogito.jobs.messaging.quarkus.AbstractReactiveMessagingJobsServiceTest;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.reactivemessaging.http.runtime.OutgoingHttpMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.jobs.knative.eventing.quarkus.KnativeEventingJobsService.CLOUD_EVENTS_CONTENT_TYPE;

class KnativeEventingJobsServiceTest extends AbstractReactiveMessagingJobsServiceTest<KnativeEventingJobsService> {

    @Override
    protected KnativeEventingJobsService createJobsService(URI serviceUrl, ObjectMapper objectMapper, Emitter<String> eventsEmitter) {
        return new KnativeEventingJobsService(serviceUrl, objectMapper, eventsEmitter);
    }

    @Override
    protected void verifyEmitterWasInvoked() {
        super.verifyEmitterWasInvoked();
        Metadata metadata = messageCaptor.getValue().getMetadata();
        List<Object> metadataElements = StreamSupport.stream(metadata.spliterator(), false).collect(Collectors.toList());
        assertThat(metadataElements).hasSize(1);
        assertThat(metadataElements).element(0).isInstanceOf(OutgoingHttpMetadata.class);
        OutgoingHttpMetadata httpMetadata = (OutgoingHttpMetadata) metadataElements.get(0);
        assertThat(httpMetadata.getHeaders()).hasSize(1);
        List<String> contentTypeValues = httpMetadata.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        assertThat(contentTypeValues).hasSize(1);
        assertThat(contentTypeValues.get(0)).isEqualTo(CLOUD_EVENTS_CONTENT_TYPE);
    }
}

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

package org.kie.kogito.addons.quarkus.knative.eventing;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.kie.kogito.cloudevents.CloudEventUtils;
import org.kie.kogito.event.CloudEventExtensionConstants;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KnativeEventingEventMarshallerTest {

    @Test
    public void verifyMergeFromValidCeOverrides() throws JsonProcessingException, URISyntaxException {
        final ObjectMapper mapper = CloudEventUtils.Mapper.mapper();
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        KnativeEventingEventMarshaller marshaller = new KnativeEventingEventMarshaller(mapper);
        KnativeEventingEventMarshaller marshallerSpy = Mockito.spy(marshaller);

        Mockito.when(marshallerSpy.readEnvCeOverrides()).thenReturn(mapper.readTree("{ \"" + CloudEventExtensionConstants.ADDONS + "\": \"knative-eventing\"}"));
        final CloudEvent ce = new CloudEventBuilder()
                .withType("unitTest")
                .withSource(new URI("http://localhost"))
                .withDataContentType("application/json")
                .withId(UUID.randomUUID().toString())
                .withSubject("verifyMergeFromValidCeOverrides")
                .withTime(OffsetDateTime.now())
                .withExtension(CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID, "12345")
                .withData("{\"mykey\": \"myvalue\"}".getBytes(StandardCharsets.UTF_8))
                .build();
        final String ceMarshalled = marshallerSpy.marshall(ce);
        assertNotNull(ceMarshalled);
        final CloudEvent ceOverrided = mapper.readValue(ceMarshalled, CloudEvent.class);
        assertNotNull(ceOverrided);
        assertEquals("knative-eventing", ceOverrided.getExtension(CloudEventExtensionConstants.ADDONS));
        assertEquals("12345", ceOverrided.getExtension(CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID));
    }

    @Test
    public void verifyMergeFromExistingExtension() throws JsonProcessingException, URISyntaxException {
        final ObjectMapper mapper = CloudEventUtils.Mapper.mapper();
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        KnativeEventingEventMarshaller marshaller = new KnativeEventingEventMarshaller(mapper);
        KnativeEventingEventMarshaller marshallerSpy = Mockito.spy(marshaller);

        Mockito.when(marshallerSpy.readEnvCeOverrides()).thenReturn(mapper.readTree("{ \"" + CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID + "\": \"54321\"}"));
        final CloudEvent ce = new CloudEventBuilder()
                .withType("unitTest")
                .withSource(new URI("http://localhost"))
                .withDataContentType("application/json")
                .withId(UUID.randomUUID().toString())
                .withSubject("verifyMergeFromExistingExtension")
                .withTime(OffsetDateTime.now())
                .withExtension(CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID, "12345")
                .withData("{\"mykey\": \"myvalue\"}".getBytes(StandardCharsets.UTF_8))
                .build();
        final String ceMarshalled = marshallerSpy.marshall(ce);
        assertNotNull(ceMarshalled);
        final CloudEvent ceOverrided = mapper.readValue(ceMarshalled, CloudEvent.class);
        assertNotNull(ceOverrided);
        assertEquals("54321", ceOverrided.getExtension(CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID));
    }

}
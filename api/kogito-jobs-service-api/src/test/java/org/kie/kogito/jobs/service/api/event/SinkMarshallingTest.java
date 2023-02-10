/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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

package org.kie.kogito.jobs.service.api.event;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.kie.kogito.jobs.service.api.recipient.sink.SinkRecipient;
import org.kie.kogito.jobs.service.api.recipient.sink.SinkRecipientBinaryPayloadData;
import org.kie.kogito.jobs.service.api.recipient.sink.SinkRecipientJsonPayloadData;
import org.kie.kogito.jobs.service.api.serlialization.SerializationUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.BytesCloudEventData;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonCloudEventData;
import io.cloudevents.jackson.JsonFormat;

class SinkMarshallingTest {

    @Test
    void marshallBytes() throws Exception {
        CloudEvent event = CloudEventBuilder.v1()
                .withType("kogito.job.trigger")
                .withId("1")
                .withSource(URI.create("mysource"))
                .withDataContentType("application/xml")
                .withData(BytesCloudEventData.wrap("the event data".getBytes()))
                .withExtension("extension1", "evalue1")
                .build();

        byte[] marshalled = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE).serialize(event);
        System.out.println(new String(marshalled));
    }

    @Test
    void marshallJson() throws Exception {
        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("name", "walter");
        json.put("surname", "medvedeo");

        CloudEvent event = CloudEventBuilder.v1()
                .withType("kogito.job.trigger")
                .withId("1")
                .withSource(URI.create("mysource"))
                .withDataContentType("application/xml")
                .withData(JsonCloudEventData.wrap(json))
                .build();
        byte[] marshalled = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE).serialize(event);
        System.out.println(new String(marshalled));
    }

    static class User {
        String name;
        String surname;

        public User(String name, String surname) {
            this.name = name;
            this.surname = surname;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    void marshallPojo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        CloudEvent event = CloudEventBuilder.v1()
                .withType("kogito.job.trigger")
                .withId("1")
                .withSource(URI.create("mysource"))
                .withDataContentType("application/xml")
                .withData(PojoCloudEventData.wrap(new User("walter", "medvedeo"), mapper::writeValueAsBytes))
                .build();
        byte[] marshalled = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE).serialize(event);
        System.out.println(new String(marshalled));
    }

    @Test
    void sinkRecipientWithJsonPayload() throws Exception {
        SinkRecipient<SinkRecipientJsonPayloadData> recipient = buildSinkRecipientWithJsonPayload();
        System.out.println(SerializationUtils.DEFAULT_OBJECT_MAPPER.writeValueAsString(recipient));
    }

    @Test
    void sinkRecipientWithJsonPayloadToCloudEvent() throws Exception {
        SinkRecipient<SinkRecipientJsonPayloadData> recipient = buildSinkRecipientWithJsonPayload();
        System.out.println(SerializationUtils.DEFAULT_OBJECT_MAPPER.writeValueAsString(recipient));
        CloudEvent event = sinkRecipientToCloudEvent(recipient);
    }

    private static SinkRecipient<SinkRecipientJsonPayloadData> buildSinkRecipientWithJsonPayload() {
        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("name", "walter");
        json.put("surname", "medvedeo");
        SinkRecipient<SinkRecipientJsonPayloadData> recipient = SinkRecipient.builder()
                .forJsonPayload()
                .sinkUrl("http://sinkUrl")
                .contentMode(SinkRecipient.ContentMode.STRUCTURED)
                .ceSource(URI.create("http://my_source"))
                .ceEventType("kogito.timer.fired")
                .ceSubject("my_subject")
                .ceDataContentType("application/text")
                .ceDataSchema(URI.create("http://my_data_shema"))
                .ceExtension("extension1", 5)
                .ceExtension("extension2", "extension2Value")
                .payload(SinkRecipientJsonPayloadData.from(json))
                .build();
        return recipient;
    }

    @Test
    void sinkRecipientWithBinaryPayload() throws Exception {
        byte[] data = "Some bytes to send".getBytes();
        SinkRecipient<SinkRecipientBinaryPayloadData> recipient = buildSinkRecipientWithBinaryPayload(data);
        System.out.println(SerializationUtils.DEFAULT_OBJECT_MAPPER.writeValueAsString(recipient));
    }

    @Test
    void sinkRecipientWithBinaryPayloadToCloudEvent() throws Exception {
        byte[] data = "Some bytes to send".getBytes();
        SinkRecipient<SinkRecipientBinaryPayloadData> recipient = buildSinkRecipientWithBinaryPayload(data);
        System.out.println(SerializationUtils.DEFAULT_OBJECT_MAPPER.writeValueAsString(recipient));
        CloudEvent event = sinkRecipientToCloudEvent(recipient);
    }

    private static SinkRecipient<SinkRecipientBinaryPayloadData> buildSinkRecipientWithBinaryPayload(byte[] data) {
        SinkRecipient<SinkRecipientBinaryPayloadData> recipient = SinkRecipient.builder()
                .forBinaryPayload()
                .sinkUrl("http://sinkUrl")
                .contentMode(SinkRecipient.ContentMode.STRUCTURED)
                .ceSource(URI.create("http://my_source"))
                .ceEventType("kogito.timer.fired")
                .ceSubject("my_subject")
                .ceDataContentType("application/text")
                .ceDataSchema(URI.create("http://my_data_shema"))
                .ceExtension("extension1", 5)
                .ceExtension("extension2", "extension2Value")
                .payload(SinkRecipientBinaryPayloadData.from(data))
                .build();
        return recipient;
    }

    private static CloudEvent sinkRecipientToCloudEvent(SinkRecipient<?> recipient) {
        CloudEventBuilder builder = CloudEventBuilder.v1()
                .withType(recipient.getCeType())
                .withId(UUID.randomUUID().toString())
                .withSource(recipient.getCeSource());

        if (recipient.getCeDataContentType() != null) {
            builder.withDataContentType(recipient.getCeDataContentType());
        }
        if (recipient.getCeDataSchema() != null) {
            builder.withDataSchema(recipient.getCeDataSchema());
        }
        recipient.getCeExtensions().entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .forEach(entry -> builder.withExtension(entry.getKey(), entry.getValue().toString()));
        if (recipient.getPayload() != null) {
            if (recipient.getPayload().getData() instanceof byte[]) {
                builder.withData((byte[]) recipient.getPayload().getData());
            } else if (recipient.getPayload().getData() instanceof JsonNode) {
                builder.withData(JsonCloudEventData.wrap((JsonNode) recipient.getPayload().getData()));
            }
        }
        CloudEvent event = builder.build();
        byte[] marshalled = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE).serialize(event);
        System.out.println(new String(marshalled));
        return event;
    }
}

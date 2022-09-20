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
package org.kie.kogito.addon.quarkus.messaging.common;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.event.impl.AbstractCloudEventMarshaller;
import org.kie.kogito.event.impl.JacksonMarshallUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.smallrye.reactive.messaging.ce.CloudEventMetadata;

public class QuarkusCloudEventUnmarshaller extends AbstractCloudEventMarshaller<Message<?>> {

    public QuarkusCloudEventUnmarshaller(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public CloudEvent unmarshall(Message<?> message) throws IOException {
        Optional<CloudEventMetadata> metadata = message.getMetadata(CloudEventMetadata.class);
        return metadata.isPresent() ? binaryCE(metadata.get(), message.getPayload()) : JacksonMarshallUtils.unmarshall(objectMapper, message.getPayload(), CloudEvent.class);
    }

    private CloudEvent binaryCE(CloudEventMetadata<?> meta, Object payload) throws IOException {
        CloudEventBuilder builder =
                CloudEventBuilder.fromSpecVersion(SpecVersion.parse(meta.getSpecVersion()))
                        .withType(meta.getType())
                        .withSource(meta.getSource())
                        .withId(meta.getId());
        meta.getDataContentType().ifPresent(builder::withDataContentType);
        meta.getDataSchema().ifPresent(builder::withDataSchema);
        meta.getTimeStamp().map(ZonedDateTime::toOffsetDateTime).ifPresent(builder::withTime);
        meta.getSubject().ifPresent(builder::withSubject);
        meta.getExtensions().forEach((k, v) -> addExtension(builder, k, v));

        if (payload instanceof byte[]) {
            builder.withData((byte[]) payload);
        } else if (payload != null) {
            builder.withData(payload.toString().getBytes());
        }
        return builder.build();
    }

    private void addExtension(CloudEventBuilder builder, String k, Object v) {
        if (v instanceof Number) {
            builder.withExtension(k, (Number) v);
        } else if (v instanceof Boolean) {
            builder.withExtension(k, (Boolean) v);
        } else if (v instanceof byte[]) {
            builder.withExtension(k, (byte[]) v);
        } else if (v instanceof URI) {
            builder.withExtension(k, (URI) v);
        } else if (v instanceof OffsetDateTime) {
            builder.withExtension(k, (OffsetDateTime) v);
        } else {
            builder.withExtension(k, v.toString());
        }
    }
}

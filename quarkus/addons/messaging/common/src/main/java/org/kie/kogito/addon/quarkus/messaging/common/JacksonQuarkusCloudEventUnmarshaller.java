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
import java.time.ZonedDateTime;
import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;
import org.kie.kogito.event.impl.AbstractCloudEventUnmarshaller;
import org.kie.kogito.event.impl.JacksonMarshallUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData.ToBytes;
import io.smallrye.reactive.messaging.ce.CloudEventMetadata;

import static org.kie.kogito.event.cloudevents.utils.CloudEventUtils.withExtension;

public class JacksonQuarkusCloudEventUnmarshaller<T, O> extends AbstractCloudEventUnmarshaller<Message<T>, O> {

    private final ToBytes<T> toBytes;

    protected JacksonQuarkusCloudEventUnmarshaller(ObjectMapper objectMapper, Class<O> clazz, ToBytes<T> toBytes) {
        super(objectMapper, clazz);
        this.toBytes = toBytes;
    }

    @Override
    public CloudEvent unmarshall(Message<T> message) throws IOException {
        Optional<CloudEventMetadata> metadata = message.getMetadata(CloudEventMetadata.class);
        return metadata.isPresent() ? binaryCE(metadata.get(), message.getPayload()) : JacksonMarshallUtils.unmarshall(objectMapper, message.getPayload(), CloudEvent.class);
    }

    private CloudEvent binaryCE(CloudEventMetadata<?> meta, T payload) throws IOException {
        CloudEventBuilder builder =
                CloudEventBuilder.fromSpecVersion(SpecVersion.parse(meta.getSpecVersion()))
                        .withType(meta.getType())
                        .withSource(meta.getSource())
                        .withId(meta.getId());
        if (payload != null) {
            builder.withData(CloudEventUtils.fromClass(outputClass, payload, toBytes));
        }
        meta.getDataContentType().ifPresent(builder::withDataContentType);
        meta.getDataSchema().ifPresent(builder::withDataSchema);
        meta.getTimeStamp().map(ZonedDateTime::toOffsetDateTime).ifPresent(builder::withTime);
        meta.getSubject().ifPresent(builder::withSubject);
        meta.getExtensions().forEach((k, v) -> withExtension(builder, k, v));
        return builder.build();
    }
}

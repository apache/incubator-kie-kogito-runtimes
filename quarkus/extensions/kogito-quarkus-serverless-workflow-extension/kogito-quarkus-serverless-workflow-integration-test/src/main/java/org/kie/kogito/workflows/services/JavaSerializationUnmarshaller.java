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
package org.kie.kogito.workflows.services;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.time.OffsetDateTime;

import org.kie.kogito.event.CloudEventUnmarshaller;
import org.kie.kogito.event.Converter;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;

public class JavaSerializationUnmarshaller<T> implements CloudEventUnmarshaller<byte[], T> {

    private Class<T> javaDataClass;

    public JavaSerializationUnmarshaller(Class<T> javaDataClass) {
        this.javaDataClass = javaDataClass;
    }

    @Override
    public Converter<byte[], CloudEvent> cloudEvent() {
        return this::cloudEvent;
    }

    private CloudEvent cloudEvent(byte[] buffer) throws IOException {
        try (DataInputStream is = new DataInputStream(new ByteArrayInputStream(buffer))) {
            CloudEventBuilder builder = CloudEventBuilder.fromSpecVersion(SpecVersion.parse(is.readUTF()));
            builder.withId(is.readUTF());
            builder.withType(is.readUTF());
            builder.withSource(URI.create(is.readUTF()));
            boolean isPresent = is.readBoolean();
            if (isPresent) {
                builder.withTime(OffsetDateTime.parse(is.readUTF()));
            }
            isPresent = is.readBoolean();
            if (isPresent) {
                builder.withSubject(is.readUTF());
            }
            isPresent = is.readBoolean();
            if (isPresent) {
                builder.withDataSchema(URI.create(is.readUTF()));
            }
            isPresent = is.readBoolean();
            if (isPresent) {
                builder.withDataContentType(is.readUTF());
            }
            builder.withData(is.readAllBytes());
            return builder.build();
        }
    }

    @Override
    public Converter<byte[], CloudEventData> binaryCloudEvent() {
        return bytes -> PojoCloudEventData.wrap(fromBytes(bytes), JavaSerializationCloudEventDataFactory::convert);
    }

    @Override
    public Converter<CloudEventData, T> data() {
        return this::fromBytes;
    }

    private T fromBytes(CloudEventData data) throws IOException {
        if (data instanceof PojoCloudEventData) {
            Object value = ((PojoCloudEventData<T>) data).getValue();
            if (javaDataClass.isInstance(value)) {
                return javaDataClass.cast(value);
            }
        }
        return fromBytes(data.toBytes());

    }

    private T fromBytes(byte[] bytes) throws IOException {
        try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            try {
                return javaDataClass.cast(is.readObject());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}

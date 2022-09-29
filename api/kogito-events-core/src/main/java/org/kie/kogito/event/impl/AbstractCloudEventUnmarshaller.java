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
package org.kie.kogito.event.impl;

import java.io.IOException;

import org.kie.kogito.event.CloudEventUnmarshaller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEventData;
import io.cloudevents.jackson.JsonCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;

public abstract class AbstractCloudEventUnmarshaller<T> implements CloudEventUnmarshaller<T> {

    protected final ObjectMapper objectMapper;

    public AbstractCloudEventUnmarshaller(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <V> V unmarshall(CloudEventData data, Class<V> outputClass, Class<?>... parametrizedClasses) throws IOException {
        if (JsonNode.class.isAssignableFrom(outputClass)) {
            return (V) (data instanceof JsonCloudEventData ? ((JsonCloudEventData) data).getNode() : objectMapper.readTree(data.toBytes()));
        } else {
            return PojoCloudEventDataMapper.from(objectMapper, outputClass).map(data).getValue();
        }
    }
}

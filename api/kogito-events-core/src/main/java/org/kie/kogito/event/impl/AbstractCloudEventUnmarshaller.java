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

public abstract class AbstractCloudEventUnmarshaller<I, O> implements CloudEventUnmarshaller<I, O> {

    protected final ObjectMapper objectMapper;
    protected final Class<O> outputClass;

    public AbstractCloudEventUnmarshaller(ObjectMapper objectMapper, Class<O> outputClass) {
        this.objectMapper = objectMapper;
        this.outputClass = outputClass;
    }

    @Override
    public O unmarshall(CloudEventData data) throws IOException {
        if (data == null) {
            return null;
        } else if (JsonNode.class.isAssignableFrom(outputClass)) {
            return (O) (data instanceof JsonCloudEventData ? ((JsonCloudEventData) data).getNode() : objectMapper.readTree(data.toBytes()));
        } else {
            return PojoCloudEventDataMapper.from(objectMapper, outputClass).map(data).getValue();
        }
    }
}

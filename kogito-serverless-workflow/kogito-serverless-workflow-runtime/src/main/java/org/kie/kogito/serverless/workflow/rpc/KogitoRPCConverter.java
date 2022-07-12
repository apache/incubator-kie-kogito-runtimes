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
package org.kie.kogito.serverless.workflow.rpc;

import java.util.Map;
import java.util.stream.Collectors;

import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.GenericDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;

class KogitoRPCConverter implements RPCConverter {

    private static final Logger logger = LoggerFactory.getLogger(KogitoRPCConverter.class);

    @Override
    public void buildMessage(Map<String, Object> parameters, Builder builder) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                FieldDescriptor fieldDescriptor = builder.getDescriptorForType().findFieldByName(entry.getKey());
                if (fieldDescriptor != null) {
                    builder.setField(fieldDescriptor, entry.getValue());
                } else {
                    logger.info("Unrecognized parameter {}", entry.getKey());
                }
            }
        }
    }

    @Override
    public JsonNode getJsonNode(Message message) {
        return JsonObjectUtils.fromValue(message.getAllFields().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getJsonName(), this::getValue)));
    }

    private Object getValue(Map.Entry<FieldDescriptor, Object> e) {
        Object value = e.getValue();
        if (value instanceof GenericDescriptor) {
            return ((GenericDescriptor) value).getName();
        } else if (value instanceof Message) {
            return getJsonNode((Message) value);
        } else {
            return value;
        }
    }
}

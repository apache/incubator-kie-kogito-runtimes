/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.Optional;

import org.kie.kogito.correlation.Correlation;
import org.kie.kogito.correlation.CorrelationKeyResolver;
import org.kie.kogito.event.cloudevents.CloudEventExtensionConstants;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class SimpleAttributeCorrelationResolver implements CorrelationKeyResolver {

    private String referenceKey = CloudEventExtensionConstants.PROCESS_REFERENCE_ID; //can be expression
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public SimpleAttributeCorrelationResolver(String referenceKey) {
        this.referenceKey = referenceKey;
    }

    @Override
    public Correlation resolve(Object data) {
        JsonNode jsonNode = objectMapper.valueToTree(data).get(referenceKey);
        String correlationValue = Optional.ofNullable(jsonNode).map(JsonNode::asText).orElse(null);
        return new Correlation(referenceKey, correlationValue);
    }

    public static SimpleAttributeCorrelationResolver forAttribute(String attributeName) {
        return new SimpleAttributeCorrelationResolver(attributeName);
    }
}

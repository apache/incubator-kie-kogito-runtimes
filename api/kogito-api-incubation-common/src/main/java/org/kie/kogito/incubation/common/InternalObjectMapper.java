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

package org.kie.kogito.incubation.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * For internal use only (package-private).
 * Provides a method to convert an object into a given type.
 * This is an implementation detail. We may move this to a separate module in the future.
 */
public class InternalObjectMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalObjectMapper.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public static <T> T convertValue(Object self, Class<T> type) {
        if (MapLikeDataContext.class == type || MapDataContext.class == type) {
            return (T) MapDataContext.of(objectMapper.convertValue(self, Map.class));
        }
        return objectMapper.convertValue(self, type);
    }

    static Map<String, Object> convertToShallowMap(Object self) {
        JsonNode jsonNode = objectMapper.valueToTree(self);
        Map<String, Object> shallowMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        InternalFieldResolver fieldResolver = new InternalFieldResolver(self);
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> n = fields.next();
            String name = n.getKey();
            try {
                Object result = fieldResolver.resolve(name);
                shallowMap.put(name, result);
            } catch (NoSuchElementException ex) {
                LOGGER.trace("Could not resolve field descriptor for '" + name + "', continue");
            }
        }
        return shallowMap;

    }

    private InternalObjectMapper() {
    }
}

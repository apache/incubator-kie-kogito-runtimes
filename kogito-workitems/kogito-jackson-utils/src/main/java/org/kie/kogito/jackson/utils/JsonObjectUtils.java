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
package org.kie.kogito.jackson.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JsonObjectUtils {

    public static Object toJavaValue(JsonNode jsonNode) {
        if (jsonNode.isTextual()) {
            return jsonNode.asText();
        } else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        } else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        } else if (jsonNode.isDouble()) {
            return jsonNode.asDouble();
        } else if (jsonNode.isArray()) {
            Collection result = new ArrayList<>();
            for (JsonNode item : ((ArrayNode) jsonNode)) {
                result.add(toJavaValue(item));
            }
            return result;
        } else if (jsonNode.isObject()) {
            Map<String, Object> result = new HashMap<>();
            jsonNode.fields().forEachRemaining(iter -> result.put(iter.getKey(), toJavaValue(iter.getValue())));
            return result;
        } else {
            throw new IllegalArgumentException("Cannot convert node " + jsonNode);
        }
    }

    private JsonObjectUtils() {
    }

}

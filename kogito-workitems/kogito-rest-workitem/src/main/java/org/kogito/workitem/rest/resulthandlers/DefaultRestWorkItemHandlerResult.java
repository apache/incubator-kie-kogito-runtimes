/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kogito.workitem.rest.resulthandlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.kogito.workitem.rest.decorators.PrefixParamsDecorator;

import com.fasterxml.jackson.databind.JsonNode;

import io.vertx.core.json.DecodeException;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;

import static org.kogito.workitem.rest.RestWorkItemHandlerUtils.checkStatusCode;

public class DefaultRestWorkItemHandlerResult implements RestWorkItemHandlerResult {

    public static final String STATUS_CODE_PARAM = "STATUS_CODE";

    private boolean returnHeaders = false;
    private boolean returnStatusCode = false;
    private boolean failOnStatusError = true;

    public DefaultRestWorkItemHandlerResult(boolean returnHeaders, boolean returnStatusCode, boolean failOnStatusError) {
        this.returnHeaders = returnHeaders;
        this.returnStatusCode = returnStatusCode;
        this.failOnStatusError = failOnStatusError;
    }

    @Override
    public Object apply(HttpResponse<Buffer> response, Class<?> target) {
        if (this.failOnStatusError) {
            checkStatusCode(response);
        }

        Map<String, Object> result = new HashMap<>();

        try {
            Object body = target == null ? response.bodyAsJson(Map.class) : response.bodyAsJson(target);

            if (!this.returnHeaders && !this.returnStatusCode) {
                return body;
            }

            if (body instanceof Map) {
                ((Map<?, ?>) body).forEach((key, value) -> result.put(String.valueOf(key), value));
            } else if (body instanceof JsonNode && ((JsonNode) body).isObject()) {
                JsonNode node = (JsonNode) body;
                node.fields().forEachRemaining(entry -> result.put(entry.getKey(), extractJsonNodeValue(entry.getValue())));
            } else {
                result.put("body", body);
            }
        } catch (DecodeException e) {
            result.put("body", response.bodyAsString());
        }

        if (this.returnHeaders) {
            response.headers().forEach(entry -> result.put(PrefixParamsDecorator.HEADER_PREFIX + entry.getKey(), entry.getValue()));
        }
        if (this.returnStatusCode) {
            result.put(STATUS_CODE_PARAM, response.statusCode());
        }

        return result;
    }

    private static Object extractJsonNodeValue(JsonNode node) {
        if (node.isTextual())
            return node.textValue();
        if (node.isInt())
            return node.intValue();
        if (node.isLong())
            return node.longValue();
        if (node.isDouble())
            return node.doubleValue();
        if (node.isBoolean())
            return node.booleanValue();
        if (node.isNull())
            return null;
        if (node.isArray()) {
            // Wrap the Iterator in a Spliterator and create a Stream
            return StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(node.elements(), 0),
                    false)
                    .map(DefaultRestWorkItemHandlerResult::extractJsonNodeValue)
                    .collect(Collectors.toList());
        }
        if (node.isObject()) {
            // Handle objects by recursively processing each field
            Map<String, Object> result = new HashMap<>();
            node.fields().forEachRemaining(entry -> result.put(entry.getKey(), extractJsonNodeValue(entry.getValue())));
            return result;
        }
        return node.toString();
    }
}

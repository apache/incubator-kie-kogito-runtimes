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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.DecodeException;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRestWorkItemHandlerResultTest {
    @Mock
    private HttpResponse<Buffer> response;

    @Mock
    private MultiMap headers;

    private final ObjectMapper mapper = new ObjectMapper();

    // --- Helper Methods ---
    private void mockResponse(Buffer body, int statusCode, Map<String, String> headersMap) throws Exception {
        // Parse the buffer to JsonNode for consistent mocking
        JsonNode jsonNode = mapper.readTree(body.toString());

        // Mock bodyAsJson to return the parsed JSON structure
        lenient().when(response.bodyAsJson(Map.class)).thenReturn(mapper.convertValue(jsonNode, Map.class));
        lenient().when(response.bodyAsJson(Object.class)).thenReturn(jsonNode);
        lenient().when(response.bodyAsString()).thenReturn(body.toString());
        lenient().when(response.statusCode()).thenReturn(statusCode);
        lenient().when(response.headers()).thenReturn(headers);

        // Mock the Consumer-based forEach method
        lenient().doAnswer(invocation -> {
            Consumer<Map.Entry<String, String>> action = invocation.getArgument(0);
            for (Map.Entry<String, String> entry : headersMap.entrySet()) {
                action.accept(new SimpleEntry(entry));
            }
            return null;
        }).when(headers).forEach(ArgumentMatchers.<Consumer<Map.Entry<String, String>>> any());
    }

    // --- Tests for `apply` ---
    @Test
    void apply_shouldReturnBodyOnly_whenNoFlagsSet() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, false, false);
        mockResponse(Buffer.buffer("{\"key\":\"value\"}"), 200, Map.of());

        Object result = handler.apply(response, null);
        assertEquals(Map.of("key", "value"), result);
    }

    @Test
    void apply_shouldIncludeHeaders_whenReturnHeadersTrue() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(true, false, false);
        mockResponse(Buffer.buffer("{\"data\":\"test\"}"), 200, Map.of("X-Custom", "header-value"));

        Map<String, Object> result = (Map<String, Object>) handler.apply(response, null);
        System.out.printf("ADEUS " + result + "\n\n\n");
        assertEquals("test", result.get("data"));
        assertEquals("header-value", result.get("HEADER_X-Custom"));
    }

    @Test
    void apply_shouldIncludeStatusCode_whenReturnStatusCodeTrue() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, true, false);
        mockResponse(Buffer.buffer("{\"data\":\"test\"}"), 404, Map.of());

        Map<String, Object> result = (Map<String, Object>) handler.apply(response, null);
        assertEquals(404, result.get("STATUS_CODE"));
    }

    @Test
    void apply_shouldThrow_whenFailOnStatusErrorTrueAndNon2xx() {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, false, true);
        when(response.statusCode()).thenReturn(500);

        assertThrows(RuntimeException.class, () -> handler.apply(response, null));
    }

    @Test
    void apply_shouldHandleInvalidJson_whenBodyIsNotJson() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, false, false);
        when(response.bodyAsJson(Map.class)).thenThrow(new DecodeException("Invalid JSON"));
        when(response.bodyAsString()).thenReturn("raw text");

        Map<String, Object> result = (Map<String, Object>) handler.apply(response, null);
        assertEquals("raw text", result.get("body"));
    }

    @Test
    void apply_shouldExtractNestedJson_whenBodyIsComplex() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, false, false);
        String complexJson = """
                {
                    "name": "Alice",
                    "age": 30,
                    "active": true,
                    "address": {
                        "city": "Lisbon",
                        "zip": 12345
                    },
                    "tags": ["a", "b", "c"]
                }
                """;
        mockResponse(Buffer.buffer(complexJson), 200, Map.of());

        Map<String, Object> result = (Map<String, Object>) handler.apply(response, null);
        assertEquals("Alice", result.get("name"));
        assertEquals(30, result.get("age"));
        assertEquals(true, result.get("active"));
        assertEquals(Map.of("city", "Lisbon", "zip", 12345), result.get("address"));
        assertEquals(List.of("a", "b", "c"), result.get("tags"));
    }

    @Test
    void apply_shouldHandleArraysInJson_whenBodyContainsArray() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, false, false);
        String arrayJson = "{\"items\":[1,2,3]}";
        mockResponse(Buffer.buffer(arrayJson), 200, Map.of());

        Map<String, Object> result = (Map<String, Object>) handler.apply(response, null);
        assertEquals(List.of(1, 2, 3), result.get("items"));
    }

    @Test
    void apply_shouldUseTargetClass_whenTargetIsProvided() throws Exception {
        DefaultRestWorkItemHandlerResult handler = new DefaultRestWorkItemHandlerResult(false, false, false);
        TestClass testObj = new TestClass("test", 123);
        when(response.bodyAsJson(TestClass.class)).thenReturn(testObj);

        Object result = handler.apply(response, TestClass.class);
        assertTrue(result instanceof TestClass);
        assertEquals("test", ((TestClass) result).name);
        assertEquals(123, ((TestClass) result).value);
    }

    // --- Helper Classes ---
    private static class TestClass {
        private String name;
        private int value;

        public TestClass(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}

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
package org.kie.kogito.expr.jsonpath;

import org.junit.jupiter.api.Test;
import org.kie.kogito.process.workitems.impl.expr.ExpressionHandlerFactory;
import org.kie.kogito.process.workitems.impl.expr.ParsedExpression;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonPathExpressionHandlerTest {

    @Test
    void testStringExpression() {
        ParsedExpression parsedExpression = ExpressionHandlerFactory.get("jsonpath").parse("$.foo");
        JsonNode node = new ObjectMapper().createObjectNode().put("foo", "javierito");
        assertEquals("javierito", parsedExpression.eval(node, String.class));
    }

    @Test
    void testBooleanExpression() {
        ParsedExpression parsedExpression = ExpressionHandlerFactory.get("jsonpath").parse("$.foo");
        JsonNode node = new ObjectMapper().createObjectNode().put("foo", true);
        assertTrue(parsedExpression.eval(node, Boolean.class));
    }

    @Test
    void testJsonNodeExpression() {
        ParsedExpression parsedExpression = ExpressionHandlerFactory.get("jsonpath").parse("$.foo");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode compositeNode = mapper.createObjectNode().put("name", "Javierito");
        JsonNode node = mapper.createObjectNode().set("foo", compositeNode);
        assertEquals("Javierito", parsedExpression.eval(node, ObjectNode.class).get("name").asText());
    }

}

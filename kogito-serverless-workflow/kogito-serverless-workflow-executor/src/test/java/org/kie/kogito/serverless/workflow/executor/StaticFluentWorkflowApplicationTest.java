/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.serverless.workflow.executor;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;
import org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.HttpMethod;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.WireMockServer;

import io.serverlessworkflow.api.Workflow;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.functionCall;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.rest;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.inject;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

class StaticFluentWorkflowApplicationTest {

    @Test
    void helloWorld() {
        final String GREETING_STRING = "Hello World!!!";
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            Workflow workflow = workflow("HelloWorld").singleton(inject(new TextNode(GREETING_STRING)));
            assertThat(application.execute(workflow, Collections.emptyMap()).getWorkflowdata()).contains(new TextNode(GREETING_STRING));
        }
    }

    @Test
    void restInvocation() {
        WireMockServer server = new WireMockServer();
        JsonNode expectedOutput = ObjectMapperFactory.get().createObjectNode().put("name", "Javierito");
        server.stubFor(get("/name").willReturn(aResponse().withStatus(200).withJsonBody(expectedOutput)));
        final String FUNCTION_NAME = "function";
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            server.start();
            Workflow workflow = workflow("HelloRest").function(rest(FUNCTION_NAME, HttpMethod.get, "http://localhost:" + server.getOptions().portNumber() + "/name"))
                    .singleton(operation().action(functionCall(FUNCTION_NAME)));
            assertThat(application.execute(workflow, Collections.emptyMap()).getWorkflowdata()).isEqualTo(expectedOutput);
        } finally {
            server.stop();
        }
    }

    @Test
    void testExpr() {
        final String DOUBLE = "double";
        final String SQUARE = "square";
        final String HALF = "half";
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            Workflow workflow = workflow("PlayingWithExpression").function(expr(DOUBLE, ".input*=2")).function(expr(SQUARE, ".input*=.input")).function(expr(HALF, ".input/=2"))
                    .start(operation().action(functionCall(DOUBLE)).action(functionCall(SQUARE)).action(functionCall(HALF)))
                    .end(operation().outputFilter("{result:.input}")).build();
            assertThat(application.execute(workflow, Collections.singletonMap("input", 4)).getWorkflowdata().get("result").asInt()).isEqualTo(32);
        }
    }
}

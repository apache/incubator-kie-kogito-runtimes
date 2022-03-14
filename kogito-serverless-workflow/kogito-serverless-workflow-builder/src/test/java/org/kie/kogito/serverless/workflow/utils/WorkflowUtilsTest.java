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
package org.kie.kogito.serverless.workflow.utils;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;

import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.mapper.BaseObjectMapper;
import io.serverlessworkflow.api.mapper.JsonObjectMapper;
import io.serverlessworkflow.api.mapper.YamlObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.concatPaths;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.resolveFunctionMetadata;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.resolveOpenAPIMetadata;

public class WorkflowUtilsTest {

    private static final String TEST_RESOURCES = "src/test/resources";

    KogitoBuildContext context;

    @BeforeEach
    protected void setup() {
        context = JavaKogitoBuildContext.builder()
                .withApplicationProperties(new File(TEST_RESOURCES))
                .withPackageName(this.getClass().getPackage().getName())
                .build();
    }

    @Test
    public void testGetObjectMapper() {
        BaseObjectMapper objectMapper = ServerlessWorkflowUtils.getObjectMapper("json");
        assertNotNull(objectMapper);
        assertThat(objectMapper).isInstanceOf(JsonObjectMapper.class);

        objectMapper = ServerlessWorkflowUtils.getObjectMapper("yml");
        assertNotNull(objectMapper);
        assertThat(objectMapper).isInstanceOf(YamlObjectMapper.class);
    }

    @Test
    public void testResolveFunctionMetadata() {
        FunctionDefinition function = new FunctionDefinition().withName("testfunction1").withMetadata(Collections.singletonMap("testprop1", "customtestprop1val"));
        assertThat(resolveFunctionMetadata(function, "testprop1", context)).isNotNull().isEqualTo("customtestprop1val");
        assertThat(resolveFunctionMetadata(function, "testprop2", context)).isNotNull().isEqualTo("testprop2val");
    }

    @Test
    public void testResolveOpenAPIMetadata() {
        FunctionDefinition function = new FunctionDefinition().withName("testfunction1").withMetadata(Collections.singletonMap("testprop1", "customtestprop1val"));
        assertThat(resolveOpenAPIMetadata(function, "testprop1", context)).isNotNull().isEqualTo("customtestprop1val");
        assertThat(resolveOpenAPIMetadata(function, "base_path", context, String.class, "http://localhost:8080")).isEqualTo("http://localhost:8282");
        assertThat(resolveOpenAPIMetadata(function, "base_path2", context, Integer.class, 0)).isEqualTo(0);
    }

    @Test
    public void testConcatPaths() {
        final String expected = "http:localhost:8080/pepe/pepa/pepi";
        assertThat(concatPaths("http:localhost:8080/pepe/", "/pepa/pepi")).isEqualTo(expected);
        assertThat(concatPaths("http:localhost:8080/pepe", "pepa/pepi")).isEqualTo(expected);
        assertThat(concatPaths("http:localhost:8080/pepe/", "pepa/pepi")).isEqualTo(expected);
        assertThat(concatPaths("http:localhost:8080/pepe", "/pepa/pepi")).isEqualTo(expected);

    }
}

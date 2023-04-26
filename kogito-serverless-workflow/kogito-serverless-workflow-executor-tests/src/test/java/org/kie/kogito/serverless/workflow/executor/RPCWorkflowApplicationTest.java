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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.examples.sw.greeting.GreeterService;
import org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils;
import org.kie.kogito.serverless.workflow.utils.WorkflowFormat;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.grpc.Server;
import io.serverlessworkflow.api.Workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.getWorkflow;

class RPCWorkflowApplicationTest {

    private static Server server;

    @BeforeAll
    static void init() throws IOException {
        server = GreeterService.buildServer(50051);
        server.start();
    }

    @AfterAll
    static void cleanup() throws InterruptedException {
        server.shutdown();
        server.awaitTermination();
    }

    @Test
    void rpcInvocation() throws IOException {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create();
                Reader reader = new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("rpcgreet.sw.json"))) {
            Workflow workflow = ServerlessWorkflowUtils.getWorkflow(reader, WorkflowFormat.JSON);
            ObjectNode node = (ObjectNode) application.execute(workflow, Map.of("name", "John", "language", "english")).getWorkflowdata();
            assertThat(node.get("message").asText()).isEqualTo("Hello from gRPC service John");
            assertThat(node.get("state").asText()).isEqualTo("SUCCESS");
            assertThat(node.get("innerMessage").get("number").asInt()).isEqualTo(23);
            assertThat(node.get("minority").get(0).get("message").asText()).isEqualTo("marquitos");
            assertThat(node.get("minority").get(1).get("message").asText()).isEqualTo("Boungiorno Marco");
        }
    }
}

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
package org.kie.kogito.quarkus.workflows;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.test.utils.ProcessInstanceLoggingTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.path.json.JsonPath;

import jakarta.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceNotExists;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.newProcessInstance;

/**
 * Integration test to validate that process instance IDs are properly included in log messages
 * using the new ProcessInstanceContext logging framework for serverless workflows.
 */
@QuarkusIntegrationTest
class ProcessInstanceLoggingIT extends ProcessInstanceLoggingTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceLoggingIT.class);

    @Test
    void testProcessInstanceIdInLogsUsingGreetWorkflow() throws IOException {
        LOGGER.info("Testing process instance logging format with MDC using greet workflow");

        // Get the Quarkus log file path and clear it
        Path logFile = getLogFilePath();
        clearLogFile(logFile);

        // Use greet workflow - a switch-based workflow with conditional logic
        String processInput = buildGreetWorkflowInput("TestUser", "English");
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

        // Create multiple process instances to test context isolation
        JsonPath jsonPath1 = newProcessInstance("/greet", processInput, headers);
        String processInstanceId1 = jsonPath1.getString("id");
        assertThat(processInstanceId1).isNotBlank();

        JsonPath jsonPath2 = newProcessInstance("/greet",
                buildGreetWorkflowInput("SecondUser", "Spanish"), headers);
        String processInstanceId2 = jsonPath2.getString("id");
        assertThat(processInstanceId2).isNotBlank();

        // Wait for workflows to complete
        waitForWorkflowCompletion("/greet", processInstanceId1, Duration.ofSeconds(10));
        waitForWorkflowCompletion("/greet", processInstanceId2, Duration.ofSeconds(10));

        // Verify both workflows completed successfully
        assertProcessInstanceNotExists("/greet/{id}", processInstanceId1);
        assertProcessInstanceNotExists("/greet/{id}", processInstanceId2);

        // Validate the log format and process instance context
        validateProcessInstanceLogs(logFile, processInstanceId1, processInstanceId2);
    }

    @Test
    void testProcessInstanceIdInLogsUsingHelloWorldWorkflow() throws IOException {
        LOGGER.info("Testing process instance logging format using simple helloworld workflow");

        // Get the Quarkus log file path and clear it
        Path logFile = getLogFilePath();
        clearLogFile(logFile);

        // Use helloworld workflow - simple inject state workflow
        String processInput = buildEmptyWorkflowInput();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");

        // Create a process instance
        JsonPath jsonPath = newProcessInstance("/helloworld", processInput, headers);
        String processInstanceId = jsonPath.getString("id");
        assertThat(processInstanceId).isNotBlank();

        // Wait for workflow to complete
        waitForWorkflowCompletion("/helloworld", processInstanceId, Duration.ofSeconds(10));

        // Verify workflow completed successfully
        assertProcessInstanceNotExists("/helloworld/{id}", processInstanceId);

        // Validate the log format and process instance context
        validateProcessInstanceLogs(logFile, processInstanceId);
    }

    @Test
    void testGeneralContextInNonProcessLogs() throws IOException, InterruptedException {
        LOGGER.info("Testing general context for non-process-specific logs");

        // Get the Quarkus log file path and clear it
        Path logFile = getLogFilePath();
        clearLogFile(logFile);

        // Make some application-level calls that should use empty context
        given()
                .contentType("application/json")
                .when()
                .get("/q/health")
                .then()
                .statusCode(200);

        // Wait longer for background services to generate logs
        // General context logs are typically generated by background services like Kafka clients
        waitForLogFlush(3000);

        // Validate that some logs use empty context
        validateGeneralContextLogs(logFile);
    }

}

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
package org.kie.kogito.quarkus.token.persistence.workflows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kie.kogito.test.utils.ProcessInstanceLogAnalyzer;
import org.kie.kogito.test.utils.ProcessInstanceLoggingTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.path.json.JsonPath;

import jakarta.ws.rs.core.HttpHeaders;

import static org.kie.kogito.addons.quarkus.token.exchange.OpenApiCustomCredentialProvider.LOG_PREFIX_COMPLETED_TOKEN_EXCHANGE;
import static org.kie.kogito.addons.quarkus.token.exchange.OpenApiCustomCredentialProvider.LOG_PREFIX_FAILED_TOKEN_EXCHANGE;
import static org.kie.kogito.addons.quarkus.token.exchange.OpenApiCustomCredentialProvider.LOG_PREFIX_STARTING_TOKEN_EXCHANGE;
import static org.kie.kogito.addons.quarkus.token.exchange.cache.TokenEvictionHandler.LOG_PREFIX_FAILED_TO_REFRESH_TOKEN;
import static org.kie.kogito.addons.quarkus.token.exchange.cache.TokenEvictionHandler.LOG_PREFIX_REFRESH_COMPLETED;
import static org.kie.kogito.addons.quarkus.token.exchange.cache.TokenEvictionHandler.LOG_PREFIX_TOKEN_REFRESH;
import static org.kie.kogito.addons.quarkus.token.exchange.persistence.TokenDataStoreImpl.LOG_PREFIX_USED_REPOSITORY;
import static org.kie.kogito.quarkus.token.persistence.workflows.ExternalServiceMock.SUCCESSFUL_QUERY;
import static org.kie.kogito.quarkus.token.persistence.workflows.TokenExchangeExternalServicesMock.BASE_AND_PROPAGATED_AUTHORIZATION_TOKEN;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceNotExists;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.newProcessInstance;

@QuarkusTestResource(TokenExchangeExternalServicesMock.class)
@QuarkusTestResource(KeycloakServiceMock.class)
@QuarkusIntegrationTest
class TokenExchangeIT extends ProcessInstanceLoggingTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenExchangeIT.class);

    @Test
    void tokenExchange() throws IOException {
        LOGGER.info("Testing token exchange caching behavior - expecting 3 external service calls but only 2 token exchanges");

        // Get the Quarkus log file path and clear it
        Path logFile = getLogFilePath();
        clearLogFile(logFile);

        // Start a new process instance
        String processInput = buildTokenExchangeWorkflowInput(SUCCESSFUL_QUERY);
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, BASE_AND_PROPAGATED_AUTHORIZATION_TOKEN);

        JsonPath jsonPath = newProcessInstance("/token_exchange", processInput, headers);
        String processInstanceId = jsonPath.getString("id");
        Assertions.assertThat(processInstanceId).isNotBlank();

        // Wait for the process to complete - it should take approximately 11+ seconds
        // due to the 1s delay + 10s delay in the workflow
        long startTime = System.currentTimeMillis();
        waitForWorkflowCompletion("/token_exchange", processInstanceId, Duration.ofSeconds(25));
        long endTime = System.currentTimeMillis();

        LOGGER.info("Process completed in {} seconds", (endTime - startTime) / 1000.0);

        // Verify the process completed successfully (404 means it completed and was cleaned up)
        assertProcessInstanceNotExists("/token_exchange/{id}", processInstanceId);

        // Verify caching behavior by checking WireMock requests
        validateCachingBehavior();
        validateOAuth2LogsFromFile(logFile);

        // Verify that process instance logging format is working correctly
        validateProcessInstanceLogs(logFile, processInstanceId);
    }

    private void validateCachingBehavior() {
        List<LoggedRequest> externalServiceRequests = TokenExchangeExternalServicesMock.getInstance().findAll(
                WireMock.postRequestedFor(WireMock.urlEqualTo("/token-exchange-external-service/withExchange")));

        // Should have exactly 3 external service requests (all 3 calls to executeQueryWithExchange)
        Assertions.assertThat(externalServiceRequests)
                .as("Should have exactly 3 external service requests - one for each executeQueryWithExchange call")
                .hasSize(3);

        // Verify that all external service requests used the correct exchanged token
        for (LoggedRequest request : externalServiceRequests) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            Assertions.assertThat(authHeader)
                    .as("All external service requests should use the exchanged token")
                    .isEqualTo("Bearer KEYCLOAK_EXCHANGED_ACCESS_TOKEN");
        }
    }

    /**
     * Validate OAuth2 token exchange and caching behavior from log file
     */
    private void validateOAuth2LogsFromFile(Path logFile) throws IOException {
        List<String> logLines = Files.readAllLines(logFile);
        LOGGER.info("Analyzing {} log lines from {} for OAuth2 token exchange patterns", logLines.size(), logFile);

        Assertions.assertThat(logLines).hasSizeGreaterThan(0);

        List<String> usedJDBCRepository = logLines.stream().filter(line -> line.contains(LOG_PREFIX_USED_REPOSITORY + ": JdbcTokenCacheRepository")).toList();
        List<String> usedInMemoryRepository = logLines.stream().filter(line -> line.contains(LOG_PREFIX_USED_REPOSITORY + ": InMemoryTokenCacheRepository")).toList();
        Assertions.assertThat(usedJDBCRepository).hasSize(1);
        Assertions.assertThat(usedInMemoryRepository).hasSize(0);

        LOGGER.info("JDBC repository was used as expected");

        List<String> startTokenExchangeLogLines = logLines.stream().filter(line -> line.contains(LOG_PREFIX_STARTING_TOKEN_EXCHANGE)).toList();
        List<String> completedTokenExchangeLogLines = logLines.stream().filter(line -> line.contains(LOG_PREFIX_COMPLETED_TOKEN_EXCHANGE)).toList();
        List<String> failedTokenExchangeLogLines = logLines.stream().filter(line -> line.contains(LOG_PREFIX_FAILED_TOKEN_EXCHANGE)).toList();
        List<String> refreshTokenExchangeLogLines = logLines.stream().filter(line -> line.contains(LOG_PREFIX_TOKEN_REFRESH)).toList();
        List<String> completedRefreshTokenExchangeLogLines = logLines.stream().filter(line -> line.contains(LOG_PREFIX_REFRESH_COMPLETED)).toList();
        List<String> failedRefreshTokenExchangeLogLines = logLines.stream().filter(line -> line.contains(LOG_PREFIX_FAILED_TO_REFRESH_TOKEN)).toList();

        Assertions.assertThat(startTokenExchangeLogLines).hasSize(1);
        Assertions.assertThat(completedTokenExchangeLogLines).hasSize(1);
        Assertions.assertThat(failedTokenExchangeLogLines).hasSize(0);

        Assertions.assertThat(refreshTokenExchangeLogLines).hasSizeBetween(2, 4);
        Assertions.assertThat(completedRefreshTokenExchangeLogLines).hasSizeBetween(2, 4);
        Assertions.assertThat(failedRefreshTokenExchangeLogLines).hasSize(0);

        // Log what we found for debugging
        LOGGER.info("Token exchange analysis results:");
        LOGGER.info("  - Starting token exchange: {} times", startTokenExchangeLogLines.size());
        LOGGER.info("  - Completed token exchange: {} times", completedTokenExchangeLogLines.size());
        LOGGER.info("  - Token refresh: {} times", refreshTokenExchangeLogLines.size());
    }

    @Test
    void testConcurrentTokenExchangeLoggingSegregation() throws Exception {
        LOGGER.info("Testing concurrent token exchange with process instance logging segregation");

        // Get the Quarkus log file path and clear it
        Path logFile = getLogFilePath();
        clearLogFile(logFile);

        final int numberOfProcesses = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfProcesses);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfProcesses);
        List<Future<String>> futures = new ArrayList<>();

        try {
            // Start all processes simultaneously
            for (int i = 0; i < numberOfProcesses; i++) {
                Future<String> future = executor.submit(() -> {
                    try {
                        // Wait for the start signal
                        startLatch.await();

                        // Start a new process instance
                        String processInput = buildTokenExchangeWorkflowInput(SUCCESSFUL_QUERY);
                        Map<String, String> headers = new HashMap<>();
                        headers.put(HttpHeaders.AUTHORIZATION, BASE_AND_PROPAGATED_AUTHORIZATION_TOKEN);

                        JsonPath jsonPath = newProcessInstance("/token_exchange", processInput, headers);
                        String processInstanceId = jsonPath.getString("id");
                        LOGGER.info("Started concurrent process instance: {}", processInstanceId);

                        // Wait for the process to complete
                        waitForWorkflowCompletion("/token_exchange", processInstanceId, Duration.ofSeconds(35));

                        // Verify the process completed successfully
                        assertProcessInstanceNotExists("/token_exchange/{id}", processInstanceId);

                        completionLatch.countDown();
                        return processInstanceId;
                    } catch (Exception e) {
                        LOGGER.error("Error in concurrent process execution", e);
                        completionLatch.countDown();
                        throw new RuntimeException(e);
                    }
                });
                futures.add(future);
            }

            // Start all processes at the same time
            startLatch.countDown();

            // Wait for all processes to complete (with timeout)
            boolean completed = completionLatch.await(3, TimeUnit.MINUTES);
            Assertions.assertThat(completed)
                    .as("All concurrent processes should complete within 3 minutes")
                    .isTrue();

            // Collect all process instance IDs
            List<String> processInstanceIds = new ArrayList<>();
            for (Future<String> future : futures) {
                processInstanceIds.add(future.get());
            }

            LOGGER.info("All {} concurrent processes completed: {}", numberOfProcesses, processInstanceIds);

            // Parse and analyze the log file
            List<ProcessInstanceLogAnalyzer.LogEntry> logEntries =
                    ProcessInstanceLogAnalyzer.parseLogFileWithMultilineSupport(logFile);

            // Validate log format consistency
            ProcessInstanceLogAnalyzer.validateLogFormat(logEntries);

            // Group entries by process instance
            Map<String, List<ProcessInstanceLogAnalyzer.LogEntry>> entriesByProcess =
                    ProcessInstanceLogAnalyzer.groupByProcessInstance(logEntries);

            // Validate process instance segregation
            ProcessInstanceLogAnalyzer.validateProcessInstanceSegregation(entriesByProcess, processInstanceIds);

            // Calculate and display statistics
            ProcessInstanceLogAnalyzer.LogStatistics stats = ProcessInstanceLogAnalyzer.calculateStatistics(logEntries);
            LOGGER.info("Concurrent execution log analysis: {}", stats);

            // Validate consistent log patterns across concurrent executions (70% similarity threshold)
            ProcessInstanceLogAnalyzer.validateConsistentLogPatterns(entriesByProcess, processInstanceIds, 0.7);

            // Ensure we have logs from all expected process instances
            for (String processInstanceId : processInstanceIds) {
                List<ProcessInstanceLogAnalyzer.LogEntry> processLogs = entriesByProcess.get(processInstanceId);
                Assertions.assertThat(processLogs)
                        .as("Should have log entries for concurrent process instance " + processInstanceId)
                        .isNotEmpty();

                // Verify that all logs for this process have the correct process instance ID
                long correctlyTaggedLogs = processLogs.stream()
                        .filter(entry -> processInstanceId.equals(entry.processInstanceId))
                        .count();

                Assertions.assertThat(correctlyTaggedLogs)
                        .as("All logs for concurrent process " + processInstanceId + " should have correct process instance ID")
                        .isEqualTo(processLogs.size());
            }

            // Verify general context logs exist (should be empty process instance ID)
            List<ProcessInstanceLogAnalyzer.LogEntry> generalContextLogs = entriesByProcess.get("general");
            Assertions.assertThat(generalContextLogs)
                    .as("Should have general context logs from concurrent execution")
                    .isNotEmpty();

            LOGGER.info("Concurrent token exchange logging segregation validation completed successfully");

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

}

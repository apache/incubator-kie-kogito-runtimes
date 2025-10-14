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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @BeforeEach
    void setup() {
        TokenExchangeExternalServicesMock.getInstance().resetRequests();
    }

    @Test
    void tokenExchange() throws IOException {
        LOGGER.info("Testing token exchange caching behavior - expecting 3 external service calls but only 2 token exchanges");

        // Clear all log files including rotated ones to start with a clean slate
        clearAllLogFiles();
        Path logFile = getLogFilePath();

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
        validateOAuth2LogsFromFile(logFile, processInstanceId);

        // Verify that process instance logging format is working correctly
        // Use robust validation that handles log rotation
        validateProcessInstanceLogsRobust(processInstanceId);
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
    private void validateOAuth2LogsFromFile(Path logFile, String processInstanceId) throws IOException {
        // Read all log files to handle rotation
        List<Path> allLogFiles = getAllLogFiles();
        List<String> logLines = new ArrayList<>();

        for (Path file : allLogFiles) {
            try {
                logLines.addAll(Files.readAllLines(file));
            } catch (IOException e) {
                LOGGER.warn("Failed to read log file {}: {}", file, e.getMessage());
            }
        }

        Assertions.assertThat(logLines).hasSizeGreaterThan(0);

        LOGGER.info("Analyzing {} log lines from {} files for OAuth2 token exchange patterns for process instance {}",
                logLines.size(), allLogFiles.size(), processInstanceId);

        // Filter logs to only include those related to this process instance or general context
        // This prevents interference from concurrent tests
        List<String> processSpecificLogLines = logLines.stream()
                .filter(line -> line.contains("|" + processInstanceId + "|") || line.contains("||")) // Process-specific or general context
                .toList();

        LOGGER.info("Found {} log lines specific to process instance {} or general context",
                processSpecificLogLines.size(), processInstanceId);

        // Check repository initialization logs in all log lines (not just process-specific)
        // Repository initialization might be logged before test starts, so we check but don't fail
        List<String> usedJDBCRepository = logLines.stream().filter(line -> line.contains(LOG_PREFIX_USED_REPOSITORY + ": JdbcTokenCacheRepository")).toList();
        List<String> usedInMemoryRepository = logLines.stream().filter(line -> line.contains(LOG_PREFIX_USED_REPOSITORY + ": InMemoryTokenCacheRepository")).toList();
        Assertions.assertThat(usedJDBCRepository).hasSize(1);
        Assertions.assertThat(usedInMemoryRepository).hasSize(0);

        // Filter token exchange logs to only those related to this process instance
        List<String> startTokenExchangeLogLines = processSpecificLogLines.stream()
                .filter(line -> line.contains(LOG_PREFIX_STARTING_TOKEN_EXCHANGE))
                .toList();
        List<String> completedTokenExchangeLogLines = processSpecificLogLines.stream()
                .filter(line -> line.contains(LOG_PREFIX_COMPLETED_TOKEN_EXCHANGE))
                .toList();
        List<String> failedTokenExchangeLogLines = processSpecificLogLines.stream()
                .filter(line -> line.contains(LOG_PREFIX_FAILED_TOKEN_EXCHANGE))
                .toList();
        List<String> refreshTokenExchangeLogLines = processSpecificLogLines.stream()
                .filter(line -> line.contains(LOG_PREFIX_TOKEN_REFRESH) && line.contains(processInstanceId))
                .toList();
        List<String> completedRefreshTokenExchangeLogLines = processSpecificLogLines.stream()
                .filter(line -> line.contains(LOG_PREFIX_REFRESH_COMPLETED) && line.contains(processInstanceId))
                .toList();
        List<String> failedRefreshTokenExchangeLogLines = processSpecificLogLines.stream()
                .filter(line -> line.contains(LOG_PREFIX_FAILED_TO_REFRESH_TOKEN) && line.contains(processInstanceId))
                .toList();

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
}

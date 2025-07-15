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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.path.json.JsonPath;

import jakarta.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.kie.kogito.quarkus.workflows.ExternalServiceMock.SUCCESSFUL_QUERY;
import static org.kie.kogito.quarkus.workflows.TokenExchangeExternalServicesMock.BASE_AND_PROPAGATED_AUTHORIZATION_TOKEN;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceNotExists;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.newProcessInstance;

@QuarkusTestResource(TokenExchangeExternalServicesMock.class)
@QuarkusTestResource(KeycloakServiceMock.class)
@QuarkusIntegrationTest
class TokenExchangeIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenExchangeIT.class);

    @Test
    void tokenExchange() {
        LOGGER.info("Testing token exchange caching behavior - expecting 3 external service calls but only 2 token exchanges");

        // Start a new process instance
        String processInput = buildProcessInput(SUCCESSFUL_QUERY);
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, BASE_AND_PROPAGATED_AUTHORIZATION_TOKEN);

        JsonPath jsonPath = newProcessInstance("/token_exchange", processInput, headers);
        String processInstanceId = jsonPath.getString("id");
        Assertions.assertThat(processInstanceId).isNotBlank();

        // Wait for the process to complete - it should take approximately 11+ seconds
        // due to the 1s delay + 10s delay in the workflow
        long startTime = System.currentTimeMillis();
        waitForProcessCompletion(processInstanceId, Duration.ofSeconds(25));
        long endTime = System.currentTimeMillis();

        LOGGER.info("Process completed in {} seconds", (endTime - startTime) / 1000.0);

        // Verify the process completed successfully (404 means it completed and was cleaned up)
        assertProcessInstanceNotExists("/token_exchange/{id}", processInstanceId);

        // Verify caching behavior by checking WireMock requests
        validateCachingBehavior();
        validateCacheExpirationBehavior();
    }

    /*
     * @Test
     * void tokenExchangeMissingAuthorizationHeader() {
     * // start a new process instance by sending the post query and collect the process instance id.
     * String processInput = buildProcessInput(SUCCESSFUL_QUERY);
     * Map<String, String> headers = new HashMap<>();
     * 
     * JsonPath jsonPath = newProcessInstance("/token_exchange", processInput, headers);
     * Assertions.assertThat(jsonPath.getString("id")).isNotBlank();
     * getProcessInstance(jsonPath.getString("id"));
     * }
     */

    private void validateCachingBehavior() {
        // Get all requests made to WireMock
        List<LoggedRequest> tokenExchangeRequests = WireMock.findAll(
                WireMock.postRequestedFor(WireMock.urlEqualTo("/realms/kogito-exchange-tests/protocol/openid-connect/token")));

        List<LoggedRequest> externalServiceRequests = WireMock.findAll(
                WireMock.postRequestedFor(WireMock.urlEqualTo("/token-exchange-external-service/withExchange")));

        LOGGER.info("Found {} token exchange requests and {} external service requests",
                tokenExchangeRequests.size(), externalServiceRequests.size());

        // Verify caching behavior:
        // 1. Should have exactly 2 token exchange requests (1st call and 3rd call after cache expiration)
        Assertions.assertThat(tokenExchangeRequests)
                .as("Should have exactly 2 token exchange requests - 1st call (cache miss) and 3rd call (cache expired)")
                .hasSize(2);

        // 2. Should have exactly 3 external service requests (all 3 calls to executeQueryWithExchange)
        Assertions.assertThat(externalServiceRequests)
                .as("Should have exactly 3 external service requests - one for each executeQueryWithExchange call")
                .hasSize(3);

        // 3. Verify that all external service requests used the correct exchanged token
        for (LoggedRequest request : externalServiceRequests) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            Assertions.assertThat(authHeader)
                    .as("All external service requests should use the exchanged token")
                    .isEqualTo("Bearer KEYCLOAK_EXCHANGED_ACCESS_TOKEN");
        }

        LOGGER.info("✅ Caching behavior validated successfully:");
        LOGGER.info("  - First call: Cache miss → Token exchange performed");
        LOGGER.info("  - Second call: Cache hit → No token exchange, reused cached token");
        LOGGER.info("  - Third call: Cache expired → Token exchange performed again");
    }

    private void validateCacheExpirationBehavior() {
        // Get token exchange requests with timestamps
        List<LoggedRequest> tokenExchangeRequests = WireMock.findAll(
                WireMock.postRequestedFor(WireMock.urlEqualTo("/realms/kogito-exchange-tests/protocol/openid-connect/token")));

        Assertions.assertThat(tokenExchangeRequests)
                .as("Should have exactly 2 token exchange requests for cache expiration test")
                .hasSize(2);

        // Calculate time difference between first and second token exchange
        long firstRequestTime = tokenExchangeRequests.get(0).getLoggedDate().getTime();
        long secondRequestTime = tokenExchangeRequests.get(1).getLoggedDate().getTime();
        long timeDifferenceSeconds = (secondRequestTime - firstRequestTime) / 1000;

        LOGGER.info("Time between first and second token exchange: {} seconds", timeDifferenceSeconds);

        // Verify that the second token exchange happened after the cache expiration
        // The workflow has 1s delay + 10s delay, so the second token exchange should happen ~11 seconds later
        Assertions.assertThat(timeDifferenceSeconds)
                .as("Second token exchange should happen after cache expiration (approximately 11 seconds later)")
                .isGreaterThan(9) // Allow some tolerance
                .isLessThan(15); // But not too much tolerance

        LOGGER.info("✅ Cache expiration behavior validated successfully:");
        LOGGER.info("  - First token exchange: t=0s (cache miss)");
        LOGGER.info("  - Second token exchange: t={}s (cache expired after 10s delay)", timeDifferenceSeconds);
    }

    private void waitForProcessCompletion(String processInstanceId, Duration timeout) {
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout.toMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                // Check if process still exists - 404 means it completed and was cleaned up
                int statusCode = given()
                        .contentType("application/json")
                        .accept("application/json")
                        .when()
                        .get("/token_exchange/" + processInstanceId)
                        .then()
                        .extract()
                        .statusCode();

                if (statusCode == 404) {
                    LOGGER.info("Process instance {} completed successfully (404 - cleaned up)", processInstanceId);
                    return;
                }

                Thread.sleep(1000); // Wait 1 second before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for process completion", e);
            } catch (Exception e) {
                LOGGER.debug("Error checking process state (will retry): {}", e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for process completion", ie);
                }
            }
        }

        throw new RuntimeException("Process instance " + processInstanceId + " did not complete within " + timeout);
    }

    protected static String buildProcessInput(String query) {
        return "{\"workflowdata\": {\"query\": \"" + query + "\"} }";
    }

}

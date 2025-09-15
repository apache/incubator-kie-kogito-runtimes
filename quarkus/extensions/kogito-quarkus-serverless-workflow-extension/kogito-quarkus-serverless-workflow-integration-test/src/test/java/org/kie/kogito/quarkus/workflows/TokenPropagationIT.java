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
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.path.json.JsonPath;

import jakarta.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.kie.kogito.quarkus.workflows.ExternalServiceMock.SUCCESSFUL_QUERY;
import static org.kie.kogito.quarkus.workflows.TokenPropagationExternalServicesMock.AUTHORIZATION_TOKEN;
import static org.kie.kogito.quarkus.workflows.TokenPropagationExternalServicesMock.SERVICE3_AUTHORIZATION_TOKEN;
import static org.kie.kogito.quarkus.workflows.TokenPropagationExternalServicesMock.SERVICE3_HEADER_TO_PROPAGATE;
import static org.kie.kogito.quarkus.workflows.TokenPropagationExternalServicesMock.SERVICE4_AUTHORIZATION_TOKEN;
import static org.kie.kogito.quarkus.workflows.TokenPropagationExternalServicesMock.SERVICE4_HEADER_TO_PROPAGATE;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.newProcessInstance;

@QuarkusTestResource(TokenPropagationExternalServicesMock.class)
@QuarkusTestResource(KeycloakServiceMock.class)
@QuarkusIntegrationTest
class TokenPropagationIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenPropagationIT.class);

    @Test
    void tokenPropagations() throws InterruptedException {
        // start a new process instance by sending the post query and collect the process instance id.
        String processInput = buildProcessInput(SUCCESSFUL_QUERY);
        Map<String, String> headers = new HashMap<>();
        // prepare the headers to pass to the token_propagation SW.
        // service token-propagation-external-service1 and token-propagation-external-service2 will receive the AUTHORIZATION_TOKEN 
        headers.put(HttpHeaders.AUTHORIZATION, AUTHORIZATION_TOKEN);
        // service token-propagation-external-service3 will receive the SERVICE3_AUTHORIZATION_TOKEN
        headers.put(SERVICE3_HEADER_TO_PROPAGATE, SERVICE3_AUTHORIZATION_TOKEN);
        // service token-propagation-external-service4 will receive the SERVICE4_AUTHORIZATION_TOKEN
        headers.put(SERVICE4_HEADER_TO_PROPAGATE, SERVICE4_AUTHORIZATION_TOKEN);

        JsonPath jsonPath = newProcessInstance("/token_propagation", processInput, headers);
        String processInstanceId = jsonPath.getString("id");
        Assertions.assertThat(processInstanceId).isNotBlank();
        //Thread.sleep(20000);
        waitForProcessCompletion(processInstanceId, Duration.ofSeconds(25));
        validateExternalServiceInvocations();
    }

    protected static String buildProcessInput(String query) {
        return "{\"workflowdata\": {\"query\": \"" + query + "\"} }";
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
                        .get("/token_propagation/" + processInstanceId)
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

    private void validateExternalServiceInvocations() {
        WireMockServer wm = TokenPropagationExternalServicesMock.getInstance();

List<TokenExpectation> expectations = List.of(
    new TokenExpectation("/service1", "Bearer " + AUTHORIZATION_TOKEN),
    new TokenExpectation("/service2", "Bearer " + AUTHORIZATION_TOKEN),
    new TokenExpectation("/service3", "Bearer " + SERVICE3_AUTHORIZATION_TOKEN),
    new TokenExpectation("/service4", "Bearer " + SERVICE4_AUTHORIZATION_TOKEN),
    new TokenExpectation("/service5", "Bearer " + KeycloakServiceMock.KEYCLOAK_ACCESS_TOKEN)
);

expectations.forEach(e -> assertCalledExactlyWithAuth(wm, e.url(), e.token(), 2));
    }

    private void assertCalledExactlyWithAuth(WireMockServer wm, String url, String expectedAuthHeader, int expectedCalls) {
        var pattern = WireMock.postRequestedFor(WireMock.urlEqualTo(url));
        var requests = wm.findAll(pattern);
        Assertions.assertThat(requests)
                .as("Expected %s to be called exactly %d times", url, expectedCalls)
                .hasSize(expectedCalls);
        for (LoggedRequest req : requests) {
            Assertions.assertThat(req.getHeader(HttpHeaders.AUTHORIZATION))
                      .as("Expected Authorization header should match for %s but got '%s'", 
                       url, req.getHeader(HttpHeaders.AUTHORIZATION))
                    .isEqualTo(expectedAuthHeader);
        }
    }
}

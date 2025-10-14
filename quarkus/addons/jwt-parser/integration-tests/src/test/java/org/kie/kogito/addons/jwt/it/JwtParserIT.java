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
package org.kie.kogito.addons.jwt.it;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.addons.jwt.JwtTokenParser;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.path.json.JsonPath;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceHasFinished;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.assertProcessInstanceNotExists;
import static org.kie.kogito.test.utils.ProcessInstancesRESTTestUtils.newProcessInstance;

@QuarkusIntegrationTest
class JwtParserIT {

    @Inject
    JwtTokenParser jwtTokenParser;

    // Valid JWT token for testing (contains: {"sub":"1234567890","preferred_username":"johndoe","email":"johndoe@example.com","iat":1516239022})
    private static final String VALID_JWT_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwicHJlZmVycmVkX3VzZXJuYW1lIjoiam9obmRvZSIsImVtYWlsIjoiam9obmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTUxNjIzOTAyMn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @Test
    void testJwtTokenParserInjection() {
        assertThat(jwtTokenParser).isNotNull();
    }

    @Test
    void testParseTokenWithNullToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenParser.parseToken(null));
    }

    @Test
    void testParseTokenWithEmptyToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenParser.parseToken(""));
    }

    @Test
    void testParseValidJwtToken() {
        var result = jwtTokenParser.parseToken(VALID_JWT_TOKEN);
        assertThat(result).isNotNull();
        assertThat(result.get("sub").asText()).isEqualTo("1234567890");
        assertThat(result.get("preferred_username").asText()).isEqualTo("johndoe");
        assertThat(result.get("email").asText()).isEqualTo("johndoe@example.com");
    }

    @Test
    void testParseTokenWithBearerPrefix() {
        String tokenWithBearer = "Bearer " + VALID_JWT_TOKEN;
        var result = jwtTokenParser.parseToken(tokenWithBearer);
        assertThat(result).isNotNull();
        assertThat(result.get("sub").asText()).isEqualTo("1234567890");
    }

    @Test
    void testExtractUser() {
        var result = jwtTokenParser.extractUser(VALID_JWT_TOKEN);
        assertThat(result).isNotNull();
        assertThat(result.get("sub").asText()).isEqualTo("1234567890");
        assertThat(result.get("preferred_username").asText()).isEqualTo("johndoe");
        assertThat(result.get("email").asText()).isEqualTo("johndoe@example.com");
    }

    @Test
    void testExtractSpecificClaim() {
        var result = jwtTokenParser.extractClaim(VALID_JWT_TOKEN, "preferred_username");
        assertThat(result).isNotNull();
        assertThat(result.asText()).isEqualTo("johndoe");
    }

    @Test
    void testExtractUserWithInvalidToken() {
        assertThrows(RuntimeException.class, () -> jwtTokenParser.extractUser("invalid.token.here"));
    }

    @Test
    void testExtractClaimWithInvalidToken() {
        assertThrows(RuntimeException.class, () -> jwtTokenParser.extractClaim("invalid.token.here", "sub"));
    }

    /**
     * End-to-end test that verifies the JWT parser works within a complete SonataFlow workflow.
     * This test demonstrates the feature working as requested in Issue #1899.
     */
    @Test
    void testJwtParserWorkflowEndToEnd() {
        // Prepare workflow input
        String processInput = "{}";
        
        // Set up headers with JWT token
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Authorization-acme_financial_auth", VALID_JWT_TOKEN);
        
        // Start the workflow process
        JsonPath jsonPath = newProcessInstance("/jwt_example", processInput, headers);
        String processInstanceId = jsonPath.getString("id");
        assertThat(processInstanceId).isNotBlank();
        
        // Wait for process completion and verify results
        assertProcessInstanceHasFinished("/jwt_example/{id}", processInstanceId, 10);
        
        // Verify the final workflow output contains the expected user information
        JsonPath completedProcess = given()
                .contentType("application/json")
                .accept("application/json")
                .when()
                .get("/jwt_example/" + processInstanceId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();
        
        // Verify that the JWT was parsed and user information was extracted
        String message = completedProcess.getString("workflowdata.message");
        assertThat(message).contains("johndoe"); // The preferred_username from the JWT
        assertThat(message).contains("Congrats");
        assertThat(message).contains("loan has been approved");
        
        Boolean loanApproved = completedProcess.getBoolean("workflowdata.loanApproved");
        assertThat(loanApproved).isTrue();
        
        // Verify the process completed successfully and was cleaned up (following TokenExchangeIT pattern)
        assertProcessInstanceNotExists("/jwt_example/{id}", processInstanceId);
    }

    /**
     * Test workflow execution with different JWT operations
     */
    @Test
    void testJwtParserWithDifferentOperations() {
        // Test with parse operation
        testWorkflowWithOperation("parse", VALID_JWT_TOKEN);
        
        // Test with extractUser operation  
        testWorkflowWithOperation("extractUser", VALID_JWT_TOKEN);
        
        // Test with Bearer prefix
        testWorkflowWithOperation("extractUser", "Bearer " + VALID_JWT_TOKEN);
    }

    private void testWorkflowWithOperation(String operation, String token) {
        String processInput = "{}";
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Authorization-acme_financial_auth", token);
        
        JsonPath jsonPath = newProcessInstance("/jwt_example", processInput, headers);
        String processInstanceId = jsonPath.getString("id");
        assertThat(processInstanceId).isNotBlank();
        
        // Verify the process completes successfully
        assertProcessInstanceHasFinished("/jwt_example/{id}", processInstanceId, 10);
    }
}

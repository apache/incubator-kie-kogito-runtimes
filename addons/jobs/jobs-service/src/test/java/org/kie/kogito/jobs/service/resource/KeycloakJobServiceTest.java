/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.jobs.service.resource;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.keycloak.representations.AccessTokenResponse;
import org.kie.kogito.jobs.api.Job;
import org.kie.kogito.jobs.api.JobBuilder;
import org.kie.kogito.jobs.service.model.JobStatus;
import org.kie.kogito.jobs.service.model.ScheduledJob;
import org.kie.kogito.jobs.service.utils.DateUtil;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(KeycloakServerTestResource.class)
@QuarkusTestResource(InfinispanServerTestResource.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KeycloakJobServiceTest {

    private static final String KEYCLOAK_SERVER_URL = System.getProperty("keycloak.url", "http://localhost:8281/auth");
    private static final String KEYCLOAK_REALM = "kogito";
    private static final String KEYCLOAK_CLIENT_ID = "kogito-jobs-service";

    public static final int OK_CODE = 200;
    public static final int UNAUTHORIZED_CODE = 403;
    public static final int FORBIDDEN_CODE = 401;

    @BeforeAll
    public static void setup() {
        System.setProperty("quarkus.http.auth.policy.role-policy1.roles-allowed", "confidential");
        System.setProperty("quarkus.http.auth.permission.roles1.paths", "/*");
        System.setProperty("quarkus.http.auth.permission.roles1.policy", "role-policy1");
    }

    @Inject
    private ObjectMapper objectMapper;

    @Test
    void create() throws Exception {
        final Job job = getJob("1");
        createJob(jobToJson(job), getAccessToken("alice"), UNAUTHORIZED_CODE);
        createJob(jobToJson(job), "", FORBIDDEN_CODE);
        final ScheduledJob response = createJob(jobToJson(job), getAccessToken("jdoe"), OK_CODE)
                .extract()
                .as(ScheduledJob.class);
        assertEquals(job, response);
    }

    private ValidatableResponse createJob(String body, String token, int statusCode) throws IOException {
        RequestSpecification requestSpecification = given();
        if (token != null && !token.isEmpty()) {
            requestSpecification = requestSpecification.given().auth().oauth2(token);
        }
        return requestSpecification
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(JobResource.JOBS_PATH)
                .then()
                .statusCode(statusCode);
    }

    private String jobToJson(Job job) throws JsonProcessingException {
        return objectMapper.writeValueAsString(job);
    }

    private Job getJob(String id) {
        return JobBuilder
                .builder()
                .id(id)
                .expirationTime(DateUtil.now().plusSeconds(10))
                .callbackEndpoint("http://localhost:8081/callback")
                .priority(1)
                .build();
    }

    @Test
    void deleteAfterCreate() throws Exception {
        final String id = "2";
        final Job job = getJob(id);
        createJob(jobToJson(job), getAccessToken("alice"), UNAUTHORIZED_CODE);
        createJob(jobToJson(job), "", FORBIDDEN_CODE);
        createJob(jobToJson(job), getAccessToken("jdoe"), OK_CODE);
        given().pathParam("id", id)
                .when()
                .delete(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(FORBIDDEN_CODE);
        given().auth().oauth2(getAccessToken("alice"))
                .pathParam("id", id)
                .when()
                .delete(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(UNAUTHORIZED_CODE);

        final ScheduledJob response = given().auth().oauth2(getAccessToken("jdoe"))
                .pathParam("id", id)
                .when()
                .delete(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(OK_CODE)
                .contentType(ContentType.JSON)
                .extract()
                .as(ScheduledJob.class);
        assertEquals(job, response);
    }

    @Test
    void getAfterCreate() throws Exception {
        final String id = "3";
        final Job job = getJob(id);
        createJob(jobToJson(job), getAccessToken("jdoe"), OK_CODE);
        given().pathParam("id", id)
                .when()
                .get(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(FORBIDDEN_CODE);
        given().auth().oauth2(getAccessToken("alice"))
                .pathParam("id", id)
                .when()
                .get(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(UNAUTHORIZED_CODE);
        final ScheduledJob scheduledJob = given().auth().oauth2(getAccessToken("jdoe"))
                .pathParam("id", id)
                .when()
                .get(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(OK_CODE)
                .contentType(ContentType.JSON)
                .assertThat()
                .extract()
                .as(ScheduledJob.class);
        assertEquals(scheduledJob.getId(), job.getId());
    }

    @Test
    void executeTest() throws Exception {
        final String id = "4";
        final Job job = getJob(id);
        createJob(jobToJson(job), getAccessToken("jdoe"), OK_CODE);
        given().pathParam("id", id)
                .when()
                .get(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(FORBIDDEN_CODE);
        given().auth().oauth2(getAccessToken("alice"))
                .pathParam("id", id)
                .when()
                .get(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(UNAUTHORIZED_CODE);
        final ScheduledJob scheduledJob = given().auth().oauth2(getAccessToken("jdoe"))
                .pathParam("id", id)
                .when()
                .get(JobResource.JOBS_PATH + "/{id}")
                .then()
                .statusCode(OK_CODE)
                .contentType(ContentType.JSON)
                .assertThat()
                .extract()
                .as(ScheduledJob.class);
        assertEquals(scheduledJob.getId(), job.getId());
        assertEquals(0, scheduledJob.getRetries());
        assertEquals(JobStatus.SCHEDULED, scheduledJob.getStatus());
        assertNotNull(scheduledJob.getScheduledId());
    }

    private String getAccessToken(String userName) {
        return given()
                .param("grant_type", "password")
                .param("username", userName)
                .param("password", userName)
                .param("client_id", KEYCLOAK_CLIENT_ID)
                .param("client_secret", "secret")
                .when()
                .post(KEYCLOAK_SERVER_URL + "/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token")
                .as(AccessTokenResponse.class).getToken();
    }
}
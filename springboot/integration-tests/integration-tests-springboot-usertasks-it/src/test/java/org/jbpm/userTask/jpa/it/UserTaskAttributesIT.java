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

package org.jbpm.userTask.jpa.it;

import java.util.Collections;
import java.util.Set;

import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kie.kogito.it.KogitoSpringbootApplication;
import org.kie.kogito.testcontainers.springboot.PostgreSqlSpringBootTestResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
@ContextConfiguration(initializers = PostgreSqlSpringBootTestResource.class)
public class UserTaskAttributesIT extends BaseUserTaskIT {

    public static final String ADMIN_USERS_RESOURCE = "adminUsers";
    public static final String ADMIN_GROUPS_RESOURCE = "adminGroups";
    public static final String POTENTIAL_USERS_RESOURCE = "potentialUsers";
    public static final String EXCLUDED_USERS_RESOURCE = "excludedUsers";

    private String pid;

    @BeforeEach
    public void setUp() {
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American", new Address("main street", "Boston", "10005", "US"));

        pid = startProcessInstance(traveller);
    }

    @AfterEach
    public void tearDown() {
        abortProcessInstance(pid);
        pid = "";
    }

    @ParameterizedTest
    @ValueSource(strings = { ADMIN_USERS_RESOURCE, ADMIN_GROUPS_RESOURCE, POTENTIAL_USERS_RESOURCE, EXCLUDED_USERS_RESOURCE })
    public void testUserTaskUsersAndGroupAttributes(String input) {
        /* Set up */
        String taskId = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .extract()
                .path("[0].id");

        /* test PUT */
        setAndVerify(input, taskId, Set.of("Replacement" + input + "1", "Replacement" + input + "2"));
        setAndVerify(input, taskId, Set.of("Replacement" + input + "2", input + "3"));

        /* reset */
        setAndVerify(input, taskId, Collections.emptySet());

        /* test POST */
        addAndVerify(input, taskId, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2"));
        ValidatableResponse response = addAndVerify(input, taskId, Set.of(input.toLowerCase() + "2", input + "4"));
        verifyResponseResourceContainsExactly(response, input, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2", input + "4"));

        /* test DELETE */
        setAndVerify(input, taskId, Set.of("Replacement" + input + "2", input + "3"));
        addAndVerify(input, taskId, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2"));

        response = removeAndVerify(input, taskId, Set.of(input.toUpperCase() + "1"));
        verifyResponseResourceContainsExactly(response, input, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2", "Replacement" + input + "2", input + "3"));

        response = removeAndVerify(input, taskId, Set.of(input.toLowerCase() + "1", "Replacement" + input + "2"));
        verifyResponseResourceContainsExactly(response, input, Set.of(input.toLowerCase() + "2", input + "3"));
    }

    private ValidatableResponse addAndVerify(String resource, String taskId, Set<String> entries) {
        ValidatableResponse response = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .body(entries)
                .post(USER_TASKS_INSTANCE_ENDPOINT + "/" + resource, taskId)
                .then();
        response.statusCode(200)
                .body("id", not(emptyOrNullString()))
                .body(resource, hasSize(greaterThanOrEqualTo(entries.size())))
                .body(resource, hasItems(entries.toArray()));
        return response;
    }

    private ValidatableResponse setAndVerify(String resource, String taskId, Set<String> entries) {
        ValidatableResponse response = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .body(entries)
                .put(USER_TASKS_INSTANCE_ENDPOINT + "/" + resource, taskId)
                .then();
        response.statusCode(200)
                .body("id", not(emptyOrNullString()))
                .body(resource, containsInAnyOrder(entries.toArray()))
                .body(resource, hasSize(entries.size()));
        return response;
    }

    private ValidatableResponse removeAndVerify(String resource, String taskId, Set<String> adminUsersToRemove) {
        ValidatableResponse response = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .body(adminUsersToRemove)
                .delete(USER_TASKS_INSTANCE_ENDPOINT + "/" + resource, taskId)
                .then();
        response.statusCode(200)
                .body("id", not(emptyOrNullString()))
                .body(resource, not(containsInAnyOrder(adminUsersToRemove.toArray())));
        return response;
    }

    private void verifyResponseResourceContainsExactly(ValidatableResponse response, String resource, Set<String> shouldContain) {
        response.body(resource, hasSize(shouldContain.size()))
                .body(resource, containsInAnyOrder(shouldContain.toArray()));
    }

}

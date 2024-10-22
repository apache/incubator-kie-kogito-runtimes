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

import java.util.Map;

import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.quarkus.PostgreSqlQuarkusTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusIntegrationTest
@QuarkusTestResource(value = PostgreSqlQuarkusTestResource.class, restrictToAnnotatedClass = true)
public class UserTaskLifeCycleIT extends BaseUserTaskIT {
    public static final String USER_TASKS_INSTANCE_TRANSITION_ENDPOINT = USER_TASKS_INSTANCE_ENDPOINT + "/transition";

    static {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @Test
    public void testUserTaskLifeCycle() {

        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American", new Address("main street", "Boston", "10005", "US"));

        final String pid = startProcessInstance(traveller);

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

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .get(USER_TASKS_INSTANCE_ENDPOINT, taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId))
                .body("status.name", equalTo("Reserved"))
                .body("taskName", equalTo("firstLineApproval"))
                .body("potentialUsers", hasItem("manager"))
                .body("potentialGroups", hasItem("managers"))
                .body("inputs.traveller.firstName", equalTo(traveller.getFirstName()))
                .body("inputs.traveller.lastName", equalTo(traveller.getLastName()))
                .body("inputs.traveller.email", equalTo(traveller.getEmail()))
                .body("inputs.traveller.nationality", equalTo(traveller.getNationality()))
                .body("metadata.ProcessType", equalTo("BPMN"))
                .body("metadata.ProcessVersion", equalTo("1.0"))
                .body("metadata.ProcessId", equalTo(PROCESS_ID))
                .body("metadata.ProcessInstanceId", equalTo(pid))
                .body("metadata.ProcessInstanceState", equalTo(1));

        given()
                .contentType(ContentType.JSON)
                .when()
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("transitionId", "complete")
                .body(Map.of("approved", true))
                .post(USER_TASKS_INSTANCE_TRANSITION_ENDPOINT, taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId))
                .body("status.name", equalTo("Completed"))
                .body("status.terminate", equalTo("COMPLETED"))
                .body("outputs.approved", equalTo(true));

        // Manager is excluded for the secondLineApproval Task, he shouldn't be allowed to see the task
        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(0));

        taskId = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].id", not(taskId))
                .extract()
                .path("[0].id");

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .get(USER_TASKS_INSTANCE_ENDPOINT, taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId))
                .body("status.name", equalTo("Ready"))
                .body("taskName", equalTo("secondLineApproval"))
                .body("excludedUsers", hasItem("manager"))
                .body("potentialGroups", hasItem("managers"))
                .body("inputs.traveller.firstName", equalTo(traveller.getFirstName()))
                .body("inputs.traveller.lastName", equalTo(traveller.getLastName()))
                .body("inputs.traveller.email", equalTo(traveller.getEmail()))
                .body("inputs.traveller.nationality", equalTo(traveller.getNationality()))
                .body("metadata.ProcessType", equalTo("BPMN"))
                .body("metadata.ProcessVersion", equalTo("1.0"))
                .body("metadata.ProcessId", equalTo(PROCESS_ID))
                .body("metadata.ProcessInstanceId", equalTo(pid))
                .body("metadata.ProcessInstanceState", equalTo(1));

        // Manager is excluded for the secondLineApproval Task, he shouldn't be able to work on the task
        given()
                .contentType(ContentType.JSON)
                .when()
                .queryParam("user", "manager")
                .queryParam("transitionId", "claim")
                .post(USER_TASKS_INSTANCE_TRANSITION_ENDPOINT, taskId)
                .then()
                .statusCode(500);

        given()
                .contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .queryParam("transitionId", "claim")
                .post(USER_TASKS_INSTANCE_TRANSITION_ENDPOINT, taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId));

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .get(USER_TASKS_INSTANCE_ENDPOINT, taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId))
                .body("status.name", equalTo("Reserved"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .queryParam("transitionId", "complete")
                .body(Map.of("approved", true))
                .post(USER_TASKS_INSTANCE_TRANSITION_ENDPOINT, taskId)
                .then()
                .statusCode(200)
                .body("id", equalTo(taskId))
                .body("status.name", equalTo("Completed"))
                .body("status.terminate", equalTo("COMPLETED"))
                .body("outputs.approved", equalTo(true));

        given()
                .accept(ContentType.JSON)
                .when()
                .get("/{processId}/{id}", PROCESS_ID, pid)
                .then()
                .statusCode(404);
    }
}
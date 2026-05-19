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

import java.util.List;

import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.it.KogitoSpringbootApplication;
import org.kie.kogito.testcontainers.springboot.PostgreSqlSpringBootTestResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItem;

/**
 * Integration test for user task filtering functionality.
 * Tests the filter query parameters on the /usertasks/instance endpoint.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
@ContextConfiguration(initializers = PostgreSqlSpringBootTestResource.class)
public class UserTaskFilterIT extends BaseUserTaskIT {

    @AfterEach
    public void cleanUp() {
        // Get all process instances and delete them to ensure test isolation
        List<String> processInstanceIds = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/{processId}", PROCESS_ID)
                .then()
                .extract()
                .jsonPath()
                .getList("id", String.class);

        processInstanceIds.forEach(processInstanceId -> {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/{processId}/{processInstanceId}", PROCESS_ID, processInstanceId)
                    .then()
                    .statusCode(200);
        });
    }

    @Test
    public void testFilterByProcessId() {
        // Given - Start two process instances
        Traveller traveller1 = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        Traveller traveller2 = new Traveller("Jane", "Smith", "jane.smith@example.com", "Canadian",
                new Address("second street", "Toronto", "20005", "CA"));

        startProcessInstance(traveller1);
        startProcessInstance(traveller2);

        // When - Query with processId filter
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processId", PROCESS_ID)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(2))
                .body("[0].processInfo.processId", is(PROCESS_ID))
                .body("[1].processInfo.processId", is(PROCESS_ID));
    }

    @Test
    public void testFilterByProcessInstanceId() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        String pid = startProcessInstance(traveller);

        // When - Query with processInstanceId filter
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processInstanceId", pid)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].processInfo.processInstanceId", is(pid));
    }

    @Test
    public void testFilterByStatus() {
        // Given - Start a process instance (creates task in Reserved status)
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with status filter
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("status", "Reserved")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].status.name", is("Reserved"));
    }

    @Test
    public void testFilterByTaskName() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with taskName filter (case-insensitive partial match)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "Approval")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testCombinedFilters() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        String pid = startProcessInstance(traveller);

        // When - Query with multiple filters
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processId", PROCESS_ID)
                .queryParam("processInstanceId", pid)
                .queryParam("status", "Reserved")
                .queryParam("taskName", "firstLine")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].processInfo.processId", is(PROCESS_ID))
                .body("[0].processInfo.processInstanceId", is(pid))
                .body("[0].status.name", is("Reserved"))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterWithNoMatches() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with filter that doesn't match
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processId", "nonexistent")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    public void testFilterByTaskNameCaseInsensitive() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with uppercase taskName (should match case-insensitively)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "APPROVAL")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterWithIdentityAndFilters() {
        // Given - Start process instances
        Traveller traveller1 = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        Traveller traveller2 = new Traveller("Jane", "Smith", "jane.smith@example.com", "Canadian",
                new Address("second street", "Toronto", "20005", "CA"));

        String pid1 = startProcessInstance(traveller1);
        String pid2 = startProcessInstance(traveller2);

        // When - Query as manager with processInstanceId filter
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processInstanceId", pid1)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].processInfo.processInstanceId", is(pid1))
                .body("[0].potentialUsers", hasItem("manager"));
    }

    @Test
    public void testFilterWithFormatParameter() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with format=short and filters
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("format", "short")
                .queryParam("processId", PROCESS_ID)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                // Verify it's the lightweight response (UserTaskInfo)
                .body("[0].id", is(org.hamcrest.Matchers.notNullValue()))
                .body("[0].taskName", is("firstLineApproval"))
                .body("[0].status.name", is("Reserved"));
    }

    @Test
    public void testFilterByMultipleStatuses() {
        // Given - Start three process instances
        Traveller traveller1 = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        Traveller traveller2 = new Traveller("Jane", "Smith", "jane.smith@example.com", "Canadian",
                new Address("second street", "Toronto", "20005", "CA"));
        Traveller traveller3 = new Traveller("Bob", "Johnson", "bob.johnson@example.com", "British",
                new Address("third street", "London", "30005", "UK"));

        String pid1 = startProcessInstance(traveller1);
        String pid2 = startProcessInstance(traveller2);
        String pid3 = startProcessInstance(traveller3);

        // Get task IDs
        String taskId1 = given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processInstanceId", pid1)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .extract()
                .jsonPath()
                .getString("[0].id");

        String taskId2 = given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processInstanceId", pid2)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .extract()
                .jsonPath()
                .getString("[0].id");

        // Transition task1 to Ready status (using release transition)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .body("{\"transitionId\": \"release\"}")
                .when()
                .post("/usertasks/instance/{taskId}/transition", taskId1)
                .then()
                .statusCode(200);

        // Complete task2 (Reserved -> Completed)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .body("{\"transitionId\": \"complete\", \"data\": {}}")
                .when()
                .post("/usertasks/instance/{taskId}/transition", taskId2)
                .then()
                .statusCode(200);

        // Now we have: task1=Ready, task2=Completed, task3=Reserved

        // When - Query with multiple status values
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("status", "Reserved")
                .queryParam("status", "Ready")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(2))
                // Verify we get tasks with both Reserved and Ready statuses
                .body("status.name", hasItem("Reserved"))
                .body("status.name", hasItem("Ready"));
    }

    @Test
    public void testFilterByMultipleStatusesWithOtherFilters() {
        // Given - Start two process instances
        Traveller traveller1 = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        Traveller traveller2 = new Traveller("Jane", "Smith", "jane.smith@example.com", "Canadian",
                new Address("second street", "Toronto", "20005", "CA"));

        String pid1 = startProcessInstance(traveller1);
        String pid2 = startProcessInstance(traveller2);

        // When - Query with multiple statuses AND processId filter
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processId", PROCESS_ID)
                .queryParam("status", "Reserved")
                .queryParam("status", "Ready")
                .queryParam("status", "InProgress")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(2))
                .body("[0].processInfo.processId", is(PROCESS_ID))
                .body("[1].processInfo.processId", is(PROCESS_ID));
    }

    @Test
    public void testFilterByMultipleStatusesWithFormatShort() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with multiple statuses and format=short
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("format", "short")
                .queryParam("status", "Reserved")
                .queryParam("status", "Ready")
                .queryParam("status", "InProgress")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                // Verify it's the lightweight response (UserTaskInfo)
                .body("[0].id", is(org.hamcrest.Matchers.notNullValue()))
                .body("[0].taskName", is("firstLineApproval"))
                .body("[0].status.name", is("Reserved"));
    }

    @Test
    public void testFilterByTaskNamePartialMatchFromStart() {
        // Given - Start a process instance with task name "firstLineApproval"
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with partial match from start
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "first")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterByTaskNamePartialMatchFromMiddle() {
        // Given - Start a process instance with task name "firstLineApproval"
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with partial match from middle
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "Line")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterByTaskNamePartialMatchFromEnd() {
        // Given - Start a process instance with task name "firstLineApproval"
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with partial match from end
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "Approval")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterByTaskNameExactMatch() {
        // Given - Start a process instance with task name "firstLineApproval"
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with exact match
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "firstLineApproval")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterByTaskNameNoMatchWithSpace() {
        // Given - Start a process instance with task name "firstLineApproval" (camelCase, no spaces)
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with space (which doesn't exist in the stored camelCase name)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "first line")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(0)); // No match because space doesn't exist in "firstLineApproval"
    }

    @Test
    public void testFilterByTaskNameMixedCase() {
        // Given - Start a process instance with task name "firstLineApproval"
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with mixed case
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "FiRsTlInE")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterByTaskNameEmptyString() {
        // Given - Start a process instance
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with empty string (should match all tasks)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].taskName", is("firstLineApproval"));
    }

    @Test
    public void testFilterByTaskNameNonExistent() {
        // Given - Start a process instance with task name "firstLineApproval"
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with non-existent task name
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("taskName", "nonExistentTask")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }

    @Test
    public void testFilterByStatusCaseInsensitive() {
        // Given - Start a process instance (creates task in Reserved status)
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with lowercase status
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("status", "reserved")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].status.name", is("Reserved"));
    }

    @Test
    public void testFilterByStatusUpperCase() {
        // Given - Start a process instance (creates task in Reserved status)
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with uppercase status
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("status", "RESERVED")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].status.name", is("Reserved"));
    }

    @Test
    public void testFilterByStatusMixedCase() {
        // Given - Start a process instance (creates task in Reserved status)
        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        startProcessInstance(traveller);

        // When - Query with mixed case status
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("status", "ReSeRvEd")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .body("[0].status.name", is("Reserved"));
    }

    @Test
    public void testFilterByMultipleStatusesCaseInsensitive() {
        // Given - Start three process instances
        Traveller traveller1 = new Traveller("John", "Doe", "john.doe@example.com", "American",
                new Address("main street", "Boston", "10005", "US"));
        Traveller traveller2 = new Traveller("Jane", "Smith", "jane.smith@example.com", "Canadian",
                new Address("second street", "Toronto", "20005", "CA"));
        Traveller traveller3 = new Traveller("Bob", "Johnson", "bob.johnson@example.com", "British",
                new Address("third street", "London", "30005", "UK"));

        String pid1 = startProcessInstance(traveller1);
        String pid2 = startProcessInstance(traveller2);
        String pid3 = startProcessInstance(traveller3);

        // Get task IDs
        String taskId1 = given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("processInstanceId", pid1)
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .extract()
                .jsonPath()
                .getString("[0].id");

        // Transition task1 to Ready status (using release transition)
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .body("{\"transitionId\": \"release\"}")
                .when()
                .post("/usertasks/instance/{taskId}/transition", taskId1)
                .then()
                .statusCode(200);

        // Now we have: task1=Ready, task2=Reserved, task3=Reserved

        // When - Query with multiple status values in different cases
        given().contentType(ContentType.JSON)
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam("status", "reserved")
                .queryParam("status", "READY")
                .when()
                .get(USER_TASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(3))
                // Verify we get tasks with both Reserved and Ready statuses
                .body("status.name", hasItem("Reserved"))
                .body("status.name", hasItem("Ready"));
    }
}

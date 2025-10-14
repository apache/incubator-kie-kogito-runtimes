package org.jbpm.userTask.jpa.it;

import java.util.Set;

import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kie.kogito.testcontainers.quarkus.PostgreSqlQuarkusTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

@QuarkusIntegrationTest
@QuarkusTestResource(value = PostgreSqlQuarkusTestResource.class, restrictToAnnotatedClass = true)
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
        cancelProcessInstance(pid);
        pid = "";
    }

    @ParameterizedTest
    @ValueSource(strings = { ADMIN_USERS_RESOURCE, ADMIN_GROUPS_RESOURCE, POTENTIAL_USERS_RESOURCE, EXCLUDED_USERS_RESOURCE })
    public void testUserTaskUsersAndGroupAttributes(String input) {

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

        addAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2"));
        ValidatableResponse response = addAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of(input.toLowerCase() + "2", input + "4"));
        verifyResponseResourceContainsExactly(response, ADMIN_USERS_RESOURCE, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2", input + "4"));

        setAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of("Replacement" + input + "1", "Replacement" + input + "2"));
        setAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of("Replacement" + input + "2", input + "3"));

        addAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2"));

        response = removeAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of(input.toUpperCase() + "1"));
        verifyResponseResourceContainsExactly(response, ADMIN_USERS_RESOURCE, Set.of(input.toLowerCase() + "1", input.toLowerCase() + "2", "Replacement" + input + "2", input + "3"));

        response = removeAndVerify(ADMIN_USERS_RESOURCE, taskId, Set.of(input.toLowerCase() + "1", "Replacement" + input + "2"));
        verifyResponseResourceContainsExactly(response, ADMIN_USERS_RESOURCE, Set.of(input.toLowerCase() + "2", input + "3"));
    }

    private ValidatableResponse addAndVerify(String resource, String taskId, Set<String> entries) {
        ValidatableResponse response = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "john")
                .queryParam("group", "managers")
                .queryParam(resource, entries)
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
                .queryParam(resource, entries)
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
                .queryParam("user", "manager")
                .queryParam("group", "department-managers")
                .queryParam(resource, adminUsersToRemove)
                .delete(USER_TASKS_INSTANCE_ENDPOINT + "/" + resource, taskId)
                .then();
        response.statusCode(200)
                .body("id", not(emptyOrNullString()))
                .body(resource, not(containsInAnyOrder(adminUsersToRemove.toArray())));
        return response;
    }

    private void verifyResponseResourceContainsExactly(ValidatableResponse response, String resource, Set<String> shouldContain) {
        response.body(resource, containsInAnyOrder(shouldContain.toArray()));
    }
}

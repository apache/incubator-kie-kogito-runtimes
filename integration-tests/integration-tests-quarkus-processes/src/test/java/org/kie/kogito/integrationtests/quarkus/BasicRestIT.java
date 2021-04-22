/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.integrationtests.quarkus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.acme.travels.Traveller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.integrationtests.UnitOfWorkTestEventListener;
import org.kie.kogito.testcontainers.quarkus.InfinispanQuarkusTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@QuarkusTestResource(InfinispanQuarkusTestResource.Conditional.class)
class BasicRestIT {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Inject
    UnitOfWorkTestEventListener uowEventListener;

    @BeforeEach
    void resetEventListener() {
        uowEventListener.reset();
    }

    void assertExpectedUnitOfWorkEvents(Integer events) {
        assertThat(uowEventListener.getStartEvents()).hasSize(events);
        assertThat(uowEventListener.getEndEvents()).hasSize(events);
        assertThat(uowEventListener.getAbortEvents()).isEmpty();
    }

    @Test
    void testGeneratedId() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        String id = given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .post("/AdHocFragments")
                .then()
                .statusCode(201)
                .body("id", not(emptyOrNullString()))
                .body("var1", equalTo("Kermit"))
                .header("Location", not(emptyOrNullString()))
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/AdHocFragments/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("var1", equalTo("Kermit"));

        assertExpectedUnitOfWorkEvents(1);
    }

    @Test
    void testWithInaccurateModel() {

        Traveller traveller = new Traveller("Javierito", "Dimequienes", "pepe@pepe.com", "Spanish");
        String processId = given()
                .contentType(ContentType.JSON)
                .body(traveller)
                .when()
                .post("/approvals")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("processId", processId)
                .when()
                .get("/approvals/{processId}/tasks")
                .then()
                .statusCode(200);

        assertExpectedUnitOfWorkEvents(1);
    }

    @Test
    void testWithBusinessKey() {
        String businessKey = "some business key";
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        String id = given()
                .contentType(ContentType.JSON)
                .queryParam("businessKey", businessKey)
                .when()
                .body(params)
                .post("/AdHocFragments")
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", not(emptyOrNullString()))
                .body("var1", equalTo("Kermit"))
                .extract()
                .path("id");

        // UUID is no longer the BusinessKey or generated from it
        String unexpectedId = UUID.nameUUIDFromBytes(businessKey.getBytes()).toString();
        assertNotEquals(businessKey, id);
        assertNotEquals(unexpectedId, id);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/AdHocFragments/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("var1", equalTo("Kermit"));

        assertExpectedUnitOfWorkEvents(1);
    }

    @Test
    void testIdNotFound() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .get("/AdHocFragments/FOO")
                .then()
                .statusCode(404);

        assertExpectedUnitOfWorkEvents(0);
    }

    @Test
    void testUpdate() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        String id = given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .post("/AdHocFragments")
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", not(emptyOrNullString()))
                .body("var1", equalTo("Kermit"))
                .extract()
                .path("id");

        // Update the previously model
        params.put("var1", "Gonzo");
        given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .put("/AdHocFragments/{customId}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("var1", equalTo("Gonzo"));

        assertExpectedUnitOfWorkEvents(2);
    }

    @Test
    void testDelete() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        String id = given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .post("/AdHocFragments")
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", not(emptyOrNullString()))
                .body("var1", equalTo("Kermit"))
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .delete("/AdHocFragments/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("var1", equalTo("Kermit"));

        //Resource already deleted
        given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .delete("/AdHocFragments/{id}", id)
                .then()
                .statusCode(404);

        assertExpectedUnitOfWorkEvents(3);
    }

    @Test
    void testGetTasks() {
        Map<String, String> params = new HashMap<>();
        params.put("var1", "Kermit");

        String id = given()
                .contentType(ContentType.JSON)
                .when()
                .body(params)
                .post("/AdHocFragments")
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .extract()
                .path("id");

        given()
                .when()
                .get("/AdHocFragments/{id}/tasks", id)
                .then()
                .statusCode(200)
                .body("$.size", is(1))
                .body("[0].name", is("Task"));

        assertExpectedUnitOfWorkEvents(1);
    }
}

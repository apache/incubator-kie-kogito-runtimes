/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.quarkus.workflows;

import org.junit.jupiter.api.Test;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusIntegrationTest
class GreetRestIT {

    @Test
    void testGreetHiddenRest() {
        assertIt("greethidden", "Hello from JSON Workflow,");
    }

    @Test
    void testGreetRest() {
        assertIt("greet", "Hello from JSON Workflow,");
    }

    @Test
    void testGreetUnknownRest() {
        assertIt("greetUnknown", "I'm not familiar with your language,");
    }

    @Test
    void testWorkflowType() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("/greetdetails")
                .then()
                .statusCode(200)
                .body("type", is(KogitoWorkflowProcess.SW_TYPE));
    }

    @Test
    void testVersion() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("/greetdetails")
                .then()
                .statusCode(200)
                .body("version", is("1.0"));
    }

    private void assertIt(String flowName, String unknownMessage) {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"workflowdata\" : {\"name\" : \"John\", \"language\":\"English\"}}").when()
                .post("/" + flowName)
                .then()
                .statusCode(201)
                .body("workflowdata.greeting", is("Hello from JSON Workflow,"));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"workflowdata\" : {\"name\" : \"Javierito\", \"language\":\"Spanish\"}}").when()
                .post("/" + flowName)
                .then()
                .statusCode(201)
                .body("workflowdata.greeting", is("Saludos desde JSON Workflow,"));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"workflowdata\" : {\"name\" : \"John\", \"language\":\"Unknown\"}}").when()
                .post("/" + flowName)
                .then()
                .statusCode(201)
                .body("workflowdata.greeting", is(unknownMessage));
    }
}

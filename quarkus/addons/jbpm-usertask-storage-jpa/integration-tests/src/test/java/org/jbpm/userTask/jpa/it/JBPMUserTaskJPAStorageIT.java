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

import java.time.Duration;
import java.util.Map;

import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.quarkus.PostgreSqlQuarkusTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

@QuarkusIntegrationTest
@QuarkusTestResource(PostgreSqlQuarkusTestResource.class)
public class JBPMUserTaskJPAStorageIT {
    public static final Duration TIMEOUT = Duration.ofSeconds(10);
    public static final String PROCESS_ID = "approvals";
    public static final String USERTASKS_ENDPOINT = "/usertasks/instance";

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testFullProcess() {

        Traveller traveller = new Traveller("John", "Doe", "john.doe@example.com", "American", new Address("main street", "Boston", "10005", "US"));

        final String pid = given().contentType(ContentType.JSON)
                .when()
                .body(Map.of("traveller", traveller))
                .post("/{processId}", PROCESS_ID)
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", not(emptyOrNullString()))
                .extract()
                .path("id");

        System.out.println(pid);

        final String taskId = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "manager")
                .get(USERTASKS_ENDPOINT)
                .then()
                .statusCode(200)
                .body("$.size()", is(1))
                .extract()
                .path("[0].id");

        System.out.println(taskId);

    }
}

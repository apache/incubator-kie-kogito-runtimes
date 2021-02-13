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

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.quarkus.InfinispanQuarkusTestResource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@QuarkusTest
@QuarkusTestResource(InfinispanQuarkusTestResource.Conditional.class)
class JsonSchemaTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void testJsonSchema() {
        // Quarkus returns URI with "quarkus://" scheme when running via CLI and this is not compatible with
        // matchesJsonSchemaInClasspath, while matchesJsonSchema directly accepts InputStream
        InputStream jsonSchema = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/jsonSchema/approvals_firstLineApproval.json");
        assertThat(jsonSchema).isNotNull();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/approvals/firstLineApproval/schema")
                .then()
                .statusCode(200)
                .body(matchesJsonSchema(jsonSchema));
    }
}

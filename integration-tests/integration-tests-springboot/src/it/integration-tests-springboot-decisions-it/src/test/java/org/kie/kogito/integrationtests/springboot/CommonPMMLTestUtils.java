/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.integrationtests.springboot;

import java.util.Map;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.core.IsNull;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonPMMLTestUtils {

    public static void testResult(final String inputData,
                                  final String path,
                                  final String targetField,
                                  final Object expectedResult) {
        final Response response = given()
                .contentType(ContentType.JSON)
                .body(inputData)
                .when()
                .post(path);
        response.then()
                .statusCode(200)
                .body(targetField, is(expectedResult));
    }

    public static void testResultWrongData(final String inputData,
                                  final String path) {
        final Response response = given()
                .contentType(ContentType.JSON)
                .body(inputData)
                .when()
                .post(path);
        System.out.println(response.body().prettyPrint());
        response.then()
                .statusCode(500)
                .body("exception", isA(String.class));
    }

    public static void testDescriptive(final String inputData,
                                  final String basePath,
                                  final String targetField,
                                  final Map<String, Object> expectedResultMap) {
        String path = basePath + "descriptive";
        final Response response = given()
                .contentType(ContentType.JSON)
                .body(inputData)
                .when()
                .post(path);
        System.out.println(response.body().prettyPrint());
        Object resultVariables = response
                .then()
                .statusCode(200)
                .body("correlationId", is(new IsNull()))
                .body("segmentationId", is(new IsNull()))
                .body("segmentId", is(new IsNull()))
                .body("segmentIndex", is(0)) // as JSON is not schema aware, here we assert the RAW string
                .body("resultCode", is("OK"))
                .body("resultObjectName", is(targetField))
                .extract()
                .path("resultVariables");
        assertNotNull(resultVariables);
        assertTrue(resultVariables instanceof Map);
        Map<String, Object> mappedResultVariables = (Map) resultVariables;
        expectedResultMap.forEach((key, value) -> {
            assertTrue(mappedResultVariables.containsKey(key));
            assertEquals(value, mappedResultVariables.get(key));
        });

    }

    public static void testDescriptiveWrongData(final String inputData,
                                           final String basePath) {
        String path = basePath + "descriptive";
        final Response response = given()
                .contentType(ContentType.JSON)
                .body(inputData)
                .when()
                .post(path);
        System.out.println(response.body().prettyPrint());
        response.then()
                .statusCode(500)
                .body("exception", isA(String.class));
    }

}

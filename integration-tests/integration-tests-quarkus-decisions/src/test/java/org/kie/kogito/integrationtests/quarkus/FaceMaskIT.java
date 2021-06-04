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
package org.kie.kogito.integrationtests.quarkus;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Part of build certification process. Please do not remove.
 * Smoke test of kogito end-to-end scenarios.
 */
@QuarkusTest
public class FaceMaskIT {

    @Test
    public void testYoungBabyWithoutNeedForAMask() {

        final FaceMaskTestData data = new FaceMaskTestData();
        data.setHuman(new Human() {
            {
                setAge(BigDecimal.ONE);
            }
        });
        data.setLocation("Building");

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/FaceMask")
                .then()
                .statusCode(200)
                .body("'Face Mask Needed'", equalTo("No"));
    }

    @Test
    public void testSomeoneWithBreathingIssuesButInBuilding() {

        final FaceMaskTestData data = new FaceMaskTestData();
        data.setHuman(new Human() {
            {
                setName("Jane Morison");
                setAge(BigDecimal.valueOf(38));
            }
        });
        data.setLocation("Building");

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/FaceMask")
                .then()
                .statusCode(200)
                .body("'Face Mask Needed'", equalTo("Textile Mask"));
    }

    @Test
    public void testSomeoneWithBreathingIssues() {

        final FaceMaskTestData data = new FaceMaskTestData();
        data.setHuman(new Human() {
            {
                setName("Jane Morison");
                setAge(BigDecimal.valueOf(38));
            }
        });
        data.setLocation("Nature");

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/FaceMask")
                .then()
                .statusCode(200)
                .body("'Face Mask Needed'", equalTo("No"));
    }

    @Test
    public void testRespiratorNeededInABuilding() {

        final FaceMaskTestData data = new FaceMaskTestData();
        data.setHuman(new Human() {
            {
                setName("George");
                setAge(BigDecimal.valueOf(38));
            }
        });
        data.setLocation("Building");

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/FaceMask")
                .then()
                .statusCode(200)
                .body("'Face Mask Needed'", equalTo("Respirator"));
    }

    @Test
    public void testSurgicalMaskNeededInCity() {

        final FaceMaskTestData data = new FaceMaskTestData();
        data.setHuman(new Human() {
            {
                setName("George");
                setAge(BigDecimal.valueOf(38));
            }
        });
        data.setLocation("City");

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/FaceMask")
                .then()
                .statusCode(200)
                .body("'Face Mask Needed'", equalTo("Surgical Mask"));
    }
}
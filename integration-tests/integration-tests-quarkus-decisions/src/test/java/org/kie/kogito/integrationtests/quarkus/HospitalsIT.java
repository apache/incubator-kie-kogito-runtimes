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

import java.util.Arrays;
import java.util.Collections;

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
public class HospitalsIT {

    @Test
    public void testCovidPositive() {

        final HospitalsTestData data = new HospitalsTestData();
        data.setPatient(new Patient() {
            {
                setCovidPositive(true);
                setDiagnosis("tumor");
            }
        });

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/Hospitals")
                .then()
                .statusCode(200)
                .body("'Suitable Hospitals'", equalTo(Collections.singletonList("Military Hospital")));
    }

    @Test
    public void testTumor() {

        final HospitalsTestData data = new HospitalsTestData();
        data.setPatient(new Patient() {
            {
                setCovidPositive(false);
                setDiagnosis("tumor");
            }
        });

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/Hospitals")
                .then()
                .statusCode(200)
                .body("'Suitable Hospitals'", equalTo(Collections.singletonList("National Hospital")));
    }

    @Test
    public void testBrokenLeg() {

        final HospitalsTestData data = new HospitalsTestData();
        data.setPatient(new Patient() {
            {
                setCovidPositive(false);
                setDiagnosis("broken leg");
            }
        });

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/Hospitals")
                .then()
                .statusCode(200)
                .body("'Suitable Hospitals'", equalTo(Arrays.asList(
                        "National Hospital",
                        "Private Hospital",
                        "University Hospital")));
    }

    @Test
    public void testDiabetes() {

        final HospitalsTestData data = new HospitalsTestData();
        data.setPatient(new Patient() {
            {
                setCovidPositive(false);
                setDiagnosis("diabetes");
            }
        });

        given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .post("/Hospitals")
                .then()
                .statusCode(200)
                .body("'Suitable Hospitals'", equalTo(Arrays.asList(
                        "National Hospital",
                        "University Hospital")));
    }
}
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
package org.kie.kogito.it;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

@TestProfile(OptimisticLockingProfile.class)
public abstract class OptimisticLockingTest extends PersistenceTest {

    @Test
    void testParallelPersistence() {
        final String pid = given().contentType(ContentType.JSON)
                .when()
                .post("/parallel")
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", not(emptyOrNullString()))
                .extract()
                .path("id");

        await().atMost(TIMEOUT)
                .untilAsserted(() -> given().contentType(ContentType.JSON)
                        .when()
                        .get("/parallel/{id}", pid)
                        .then()
                        .statusCode(404));
    }
}

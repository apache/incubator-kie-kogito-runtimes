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

import java.util.Arrays;
import java.util.Collection;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.testcontainers.springboot.InfinispanSpringBootTestResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Part of build certification process. Please do not remove.
 * Smoke test of kogito end-to-end scenarios.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
@ContextConfiguration(initializers = InfinispanSpringBootTestResource.Conditional.class)
public class VaccinationProducersTest extends BaseRestTest {

    @Test
    public void testProducers() {

        given().contentType(ContentType.JSON)
                .when()
                .post("/Vaccination-Producers")
                .then()
                .statusCode(200)
                .body("Producers", equalTo(Arrays.asList("Glob Medicine", "Healthy Life", "Vax Med")));
    }

    @Test
    public void testProducersService() {

        given().contentType(ContentType.JSON)
                .when()
                .post("/Vaccination-Producers/ProducersService")
                .getBody()
                .as(Collection.class)
                .equals(Arrays.asList("Glob Medicine", "Healthy Life", "Vax Med"));
    }
}

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
package org.kie.kogito.integrationtests.springboot;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.springboot.KafkaSpringBootTestResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
@ContextConfiguration(initializers = { KafkaSpringBootTestResource.class })
public class MessageSenderReceiverTest extends BaseRestTest {

    @Test
    void testMessageSignalBetweenProcessInstances() throws InterruptedException {
        String pId = given().body("{ \"message\": \"hello\" }")
                .contentType(ContentType.JSON)
                .when()
                .post("/message_receiver")
                .then()
                .statusCode(201)
                .extract().body().path("id");

        validateSenderProcess(pId);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> given()
                        .contentType(ContentType.JSON)
                        .when()
                        .get("/message_receiver/{pId}", pId)
                        .then()
                        .statusCode(200)
                        .body("message", equalTo("hello world")));

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/message_receiver/{pId}/end", pId)
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/message_receiver/{pId}", pId)
                .then()
                .statusCode(404);
    }

    private void validateSenderProcess(String receiverProcessId) {

        String pId = given()
                .body("{ \"message\": \"hello\" }")
                .header("X-KOGITO-ReferenceId", receiverProcessId)
                .contentType(ContentType.JSON)
                .when()
                .post("/message_sender")
                .then()
                .statusCode(201)
                .extract().body().path("id");

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/message_sender/{pId}/continue", pId)
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/message_sender/{pId}", pId)
                .then()
                .statusCode(404);
    }
}

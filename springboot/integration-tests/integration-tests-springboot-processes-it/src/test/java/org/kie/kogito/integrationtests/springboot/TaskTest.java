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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.acme.travels.Address;
import org.acme.travels.Traveller;
import org.jbpm.util.JsonSchemaUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.kogito.task.management.service.TaskInfo;
import org.kie.kogito.usertask.model.AttachmentInfo;
import org.kie.kogito.usertask.model.CommentInfo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
public class TaskTest extends BaseRestTest {

    @Test
    void testJsonSchema() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/approvals/firstLineApproval/schema")
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("testJsonSchema/test_approvals_firstLineApproval.json"));

        Traveller traveller = new Traveller("pepe", "rubiales", "pepe.rubiales@gmail.com", "Spanish", null);

        String processId = given()
                .contentType(ContentType.JSON)
                .when()
                .body(Collections.singletonMap("traveller", traveller))
                .post("/approvals")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String taskId = given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("processId", processId)
                .when()
                .get("/approvals/{processId}/tasks")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .when()
                .get("/approvals/{processId}/firstLineApproval/{taskId}/schema", processId, taskId)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("testJsonSchema/test_approvals_firstLineApproval_instance.json"));
    }

    @Test
    void testJsonSchemaFiles() {
        long expectedJsonSchemas = 25;
        Path jsonDir = Paths.get("target", "classes").resolve(JsonSchemaUtil.getJsonDir());
        try (Stream<Path> paths = Files.walk(jsonDir)) {
            long generatedJsonSchemas = paths
                    .filter(p -> p.toString().endsWith("json"))
                    .count();
            assertThat(generatedJsonSchemas).isEqualTo(expectedJsonSchemas);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    public void testInputOutputsViaJsonTypeProperty() throws Exception {
        Traveller traveller = new Traveller("pepe", "rubiales", "pepe.rubiales@gmail.com", "Spanish", null);

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(Collections.singletonMap("traveller", traveller))
                .post("/approvals")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String taskId = given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .when()
                .get("/usertasks/instance")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        traveller = new Traveller("pepe2", "rubiales2", "pepe.rubiales@gmail.com", "Spanish2", null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTypingAsProperty(BasicPolymorphicTypeValidator.builder().build(), DefaultTyping.NON_FINAL, "@type");
        String jsonBody = mapper.writeValueAsString(Map.of("traveller", traveller));
        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .body(jsonBody)
                .put("/usertasks/instance/{taskId}/inputs")
                .then()
                .log().body()
                .statusCode(200)
                .body("inputs.traveller.firstName", is(traveller.getFirstName()))
                .body("inputs.traveller.lastName", is(traveller.getLastName()))
                .body("inputs.traveller.email", is(traveller.getEmail()))
                .body("inputs.traveller.nationality", is(traveller.getNationality()));

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .body(jsonBody)
                .put("/usertasks/instance/{taskId}/outputs")
                .then()
                .log().body()
                .statusCode(200)
                .body("outputs.traveller.firstName", is(traveller.getFirstName()))
                .body("outputs.traveller.lastName", is(traveller.getLastName()))
                .body("outputs.traveller.email", is(traveller.getEmail()))
                .body("outputs.traveller.nationality", is(traveller.getNationality()));
    }

    @Test
    void testCommentAndAttachment() {
        Traveller traveller = new Traveller("pepe", "rubiales", "pepe.rubiales@gmail.com", "Spanish", null);

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(Collections.singletonMap("traveller", traveller))
                .post("/approvals")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String taskId = given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .when()
                .get("/usertasks/instance")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        final String commentId = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .body(new CommentInfo("We need to act"))
                .post("/usertasks/instance/{taskId}/comments")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        final String commentText = "We have done everything we can";
        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("commentId", commentId)
                .body(new CommentInfo(commentText))
                .put("/usertasks/instance/{taskId}/comments/{commentId}")
                .then()
                .statusCode(200);

        assertEquals(commentText, given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("commentId", commentId)
                .get("/usertasks/instance/{taskId}/comments/{commentId}")
                .then()
                .statusCode(200).extract().path("content"));

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("commentId", commentId)
                .delete("/usertasks/instance/{taskId}/comments/{commentId}")
                .then()
                .statusCode(200);

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("commentId", commentId)
                .get("/usertasks/instance/{taskId}/comments/{commentId}")
                .then()
                .statusCode(404);

        final String attachmentId = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .body(new AttachmentInfo(URI.create("pepito.txt"), "pepito.txt"))
                .post("/usertasks/instance/{taskId}/attachments")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("attachmentId", attachmentId)
                .body(new AttachmentInfo(URI.create("file:/home/fulanito.txt")))
                .put("/usertasks/instance/{taskId}/attachments/{attachmentId}")
                .then()
                .statusCode(200);

        given().contentType(
                ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("attachmentId", attachmentId)
                .get("/usertasks/instance/{taskId}/attachments/{attachmentId}")
                .then()
                .statusCode(200).body("name", equalTo("fulanito.txt")).body("content", equalTo(
                        "file:/home/fulanito.txt"));

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("attachmentId", attachmentId)
                .delete("/usertasks/instance/{taskId}/attachments/{attachmentId}")
                .then()
                .statusCode(200);

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("attachmentId", attachmentId)
                .get("/usertasks/instance/{taskId}/attachments/{attachmentId}")
                .then()
                .statusCode(404);

        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .pathParam("attachmentId", attachmentId)
                .delete("/usertasks/instance/{taskId}/attachments/{attachmentId}")
                .then()
                .statusCode(404);
    }

    @Test
    void testSaveTask() {
        Traveller traveller = new Traveller("pepe", "rubiales", "pepe.rubiales@gmail.com", "Spanish", new Address("Alfredo Di Stefano", "Madrid", "28033", "Spain"));

        String processId = given()
                .contentType(ContentType.JSON)
                .when()
                .body(Collections.singletonMap("traveller", traveller))
                .post("/approvals")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String taskId = given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("processId", processId)
                .when()
                .get("/approvals/{processId}/tasks")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        Map<String, Object> model = Collections.singletonMap("approved", true);
        assertEquals(model, given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("processId", processId)
                .pathParam("taskId", taskId)
                .body(model)
                .put("/approvals/{processId}/firstLineApproval/{taskId}")
                .then()
                .statusCode(200)
                .extract()
                .as(Map.class));

        assertEquals(true, given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("processId", processId)
                .pathParam("taskId", taskId)
                .get("/approvals/{processId}/firstLineApproval/{taskId}")
                .then()
                .statusCode(200)
                .extract()
                .path("results.approved"));
    }

    @Test
    void testUpdateTaskInfo() {
        Traveller traveller = new Traveller("pepe", "rubiales", "pepe.rubiales@gmail.com", "Spanish", new Address("Alfredo Di Stefano", "Madrid", "28033", "Spain"));

        given()
                .contentType(ContentType.JSON)
                .when()
                .body(Collections.singletonMap("traveller", traveller))
                .post("/approvals")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String taskId = given()
                .contentType(ContentType.JSON)
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .when()
                .get("/usertasks/instance")
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        TaskInfo upTaskInfo = new TaskInfo("firstAproval", "high", Collections.singleton("admin"),
                Collections.singleton("managers"), Collections.singleton("Javierito"), Collections.emptySet(),
                Collections.emptySet(), Collections.emptyMap());
        given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .body(upTaskInfo)
                .put("/management/usertasks/{taskId}")
                .then()
                .statusCode(200);

        TaskInfo downTaskInfo = given().contentType(ContentType.JSON)
                .when()
                .queryParam("user", "admin")
                .queryParam("group", "managers")
                .pathParam("taskId", taskId)
                .get("/management/usertasks/{taskId}")
                .then()
                .statusCode(200)
                .extract()
                .as(TaskInfo.class);

        // we are only interested in our inputs
        Iterator<Map.Entry<String, Object>> iterator = downTaskInfo.getInputParams().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> item = iterator.next();
            if (!upTaskInfo.getInputParams().keySet().contains(item.getKey())) {
                iterator.remove();
            }
        }
        // we cannot compare yet because the json it is not properly deserialize
        assertThat(downTaskInfo).isEqualTo(upTaskInfo);
        assertThat(downTaskInfo.getInputParams()).isNotNull();
        assertThat(downTaskInfo.getInputParams().get("traveller")).isNull();
    }

}

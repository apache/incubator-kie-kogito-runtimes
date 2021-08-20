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
package org.kie.kogito.quarkus.rules.hotreload;

import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HotReloadIT {

    private static final String PACKAGE = "org.kie.kogito.quarkus.rules.hotreload";
    private static final String RESOURCE_FILE = PACKAGE.replace('.', '/') + "/adult.drl";
    private static final String HTTP_TEST_PORT = "65535";

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addAsResource("adult.txt", RESOURCE_FILE));

    @Test
    public void testServletChange() throws InterruptedException {
        String personsPayload = "{\"persons\":[{\"name\":\"Mario\",\"age\":45,\"adult\":false},{\"name\":\"Sofia\",\"age\":17,\"adult\":false}]}";

        List<String> names = given()
                .baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(personsPayload)
                .when()
                .post("/find-adult-names")
                .then()
                .statusCode(200)
                .extract()
                .as(List.class);

        assertEquals(1, names.size());
        assertEquals("Mario", names.get(0));

        test.modifyResourceFile(RESOURCE_FILE, s -> s.replaceAll("18", "16"));

        names = given()
                .baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(personsPayload).when()
                .post("/find-adult-names")
                .then()
                .statusCode(200)
                .extract().as(List.class);

        assertEquals(2, names.size());
        assertTrue(names.contains("Mario"));
        assertTrue(names.contains("Sofia"));
    }
}

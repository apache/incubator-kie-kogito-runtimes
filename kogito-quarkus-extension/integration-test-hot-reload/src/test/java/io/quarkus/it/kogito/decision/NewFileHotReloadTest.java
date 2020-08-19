/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.quarkus.it.kogito.decision;

import io.quarkus.it.kogito.drools.newunit.Person;
import io.quarkus.it.kogito.drools.newunit.PersonUnit;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;

public class NewFileHotReloadTest {

    private static final String PACKAGE = "io.quarkus.it.kogito.drools.newunit";
    private static final String RESOURCE_FILE_PATH = PACKAGE.replace('.', '/');
    private static final String DMN_RESOURCE_FILE = RESOURCE_FILE_PATH + "/TrafficViolation.txt";

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(Person.class)
                    .addClass(PersonUnit.class)
                    // TODO is this still needed?
                    .addAsResource("adult.txt", DMN_RESOURCE_FILE + ".dummy")); // add a dummy file only to enforce creation of reasource folder

    @Test
    public void newFileTest() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(requireNonNull(classLoader.getResource("TrafficViolation.txt")).getFile());
        String xml = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        test.addResourceFile(DMN_RESOURCE_FILE, xml);

        ValidatableResponse response = given()
                .body("{\n" +
                        "    \"Driver\": {\n" +
                        "        \"Points\": 2\n" +
                        "    },\n" +
                        "    \"Violation\": {\n" +
                        "        \"Type\": \"speed\",\n" +
                        "        \"Actual Speed\": 120,\n" +
                        "        \"Speed Limit\": 100\n" +
                        "    }\n" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/Traffic Violation")
                .then();

        // TODO to be removed/improved
        if (response.extract().statusCode() != 200) {
            System.out.println(response.extract().body().asString());
        }

        response.statusCode(200)
                .body("'Should the driver be suspended?'", is("No"));
    }
}

/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.kie.kogito.quarkus.serverless.workflow.deployment.livereload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kie.kogito.test.utils.SocketUtils;

import io.grpc.Server;
import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

public class LiveReloadProcessorTest {

    private static final int PORT = SocketUtils.findAvailablePort();

    @RegisterExtension
    public final static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .withApplicationRoot(jar -> {
                try {
                    jar.addAsResource(new StringAsset(applicationProperties()), "/application.properties");
                    jar.add(new StringAsset(new String(Files.readAllBytes(Path.of("src/main/proto/greeting.proto")))), "src/main/proto/greeting.proto");
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

    private static String applicationProperties() {
        return Stream.of(
                "quarkus.grpc.clients.Greeter.host=localhost",
                "quarkus.grpc.clients.Greeter.port=" + PORT,
                "quarkus.grpc.server.port=" + PORT,
                "quarkus.grpc.server.test-port=" + PORT)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    void testOpenApi() throws IOException {
        given()
                .contentType(ContentType.JSON)
                .accept("*/*")
                .body("{ \"workflowdata\" : { \"name\" : \"Java\" } }")
                .when()
                .post("/service").then()
                .statusCode(404);

        try (FileInputStream inputStream = new FileInputStream("src/test/resources/workflow-service.sw.json")) {
            test.addResourceFile("workflow-service.sw.json", new String(Objects.requireNonNull(inputStream).readAllBytes()));
        }

        given()
                .contentType(ContentType.JSON)
                .accept("*/*")
                .body("{ \"workflowdata\" : { \"name\" : \"Java\" } }")
                .when()
                .post("/service").then()
                .statusCode(201);

    }

    @Test
    void testEnglish() throws InterruptedException, IOException {
        Server server = GreeterService.buildServer(PORT);
        server.start();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        try {
            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"name\" : \"John\", \"language\":\"English\"}").when()
                    .post("/jsongreet")
                    .then()
                    .statusCode(404);

            try (FileInputStream inputStream = new FileInputStream("src/test/resources/rpcgreet.sw.json")) {
                test.addResourceFile("rpcgreet.sw.json", new String(Objects.requireNonNull(inputStream).readAllBytes()));
            }

            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"name\" : \"John\", \"language\":\"English\"}").when()
                    .post("/jsongreet")
                    .then()
                    .statusCode(201)
                    .body("workflowdata.message", containsString("Hello"));
        } finally {
            server.shutdownNow();
            server.awaitTermination();
        }
    }
}

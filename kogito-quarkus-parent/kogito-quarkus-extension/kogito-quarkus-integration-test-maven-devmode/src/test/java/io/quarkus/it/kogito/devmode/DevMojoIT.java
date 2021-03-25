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
package io.quarkus.it.kogito.devmode;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.Test;

import io.quarkus.maven.it.RunAndCheckMojoTestBase;
import io.quarkus.maven.it.verifier.RunningInvoker;
import io.quarkus.test.devmode.util.DevModeTestUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/*
 *  Test inspired by https://github.com/quarkusio/quarkus/blob/c8919cfb8abbc3df49dd1febd74b998417b0367e/integration-tests/maven/src/test/java/io/quarkus/maven/it/DevMojoIT.java#L218
 */
//@DisableForNative: it is not yet available as of 1.11, and I doubt is ever needed for this module
public class DevMojoIT extends RunAndCheckMojoTestBase {

    private static final String HTTP_TEST_PORT = "65535";

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private String getRestResponse(String url) throws Exception {
        AtomicReference<String> resp = new AtomicReference<>();
        // retry on exceptions for connection refused, connection errors, etc. which will occur until the Kogito Quarkus maven project is fully built and running
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> {
                    try {
                        String content = DevModeTestUtils.get("http://localhost:" + HTTP_TEST_PORT + url);
                        resp.set(content);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
        return resp.get();
    }

    /* copy-paste from quarkus */
    @Override
    protected void run(boolean performCompile, String... options) throws FileNotFoundException, MavenInvocationException {
        assertThat(testDir).isDirectory();
        running = new RunningInvoker(testDir, false);
        final List<String> args = new ArrayList<>(2 + options.length);
        if (performCompile) {
            args.add("compile");
        }
        args.add("quarkus:dev");
        boolean hasDebugOptions = false;
        for (String option : options) {
            args.add(option);
            if (option.trim().startsWith("-Ddebug=") || option.trim().startsWith("-Dsuspend=")) {
                hasDebugOptions = true;
            }
        }
        if (!hasDebugOptions) {
            // if no explicit debug options have been specified, let's just disable debugging
            args.add("-Ddebug=false");
        }

        // Since the Kogito extension split, this requires more memory, going for a default of 1GB, per surefire.
        args.add("-Djvm.args=-Xmx1024m");
        running.execute(args, Collections.emptyMap());
    }

    @Test
    public void testBPMN2HotReload() throws Exception {
        testDir = initProject("projects/classic-inst", "projects/project-intrumentation-reload-bpmn");
        run(true);

        final File controlSource = new File(testDir, "src/main/java/control/RestControl.java");

        // await Quarkus
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> getRestResponse("/control").contains("Hello, v1"));

        System.out.println("Starting bpmn process");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"s1\": \"v1\"," +
                        "    \"s2\": \"v2\"" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "simple")
                .then()
                .statusCode(201)
                .body("s2", is("Hello, v1"));

        // --- Change #1
        System.out.println("Beginning Change #1");
        File source = new File(testDir, "src/main/resources/simple.bpmn2");
        filter(source, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        filter(controlSource, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(25, TimeUnit.SECONDS).until(() -> getRestResponse("/control").contains("Ciao, v1"));

        System.out.println("Starting bpmn process");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"s1\": \"v1\"," +
                        "    \"s2\": \"v2\"" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "simple")
                .then()
                .statusCode(201)
                .body("s2", is("Ciao, v1"));

        // --- Change #2
        System.out.println("Beginning Change #2");
        filter(source, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        filter(controlSource, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(25, TimeUnit.SECONDS).until(() -> getRestResponse("/control").contains("Bonjour, v1"));

        System.out.println("Starting bpmn process");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"s1\": \"v1\"," +
                        "    \"s2\": \"v2\"" +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "simple")
                .then()
                .statusCode(201)
                .body("s2", is("Bonjour, v1"));

        System.out.println("done.");
    }

    @Test
    public void testDMNHotReload() throws Exception {
        testDir = initProject("projects/classic-inst", "projects/project-intrumentation-reload-dmn");
        run(true);

        final File controlSource = new File(testDir, "src/main/java/control/RestControl.java");

        // await Quarkus
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> getRestResponse("/control").contains("Hello, v1"));

        System.out.println("Evaluate DMN");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"v1\"   " +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "hello")
                .then()
                .statusCode(200)
                .body("greeting", is("Hello, v1"));

        // --- Change #1
        System.out.println("Beginning Change #1");
        File source = new File(testDir, "src/main/resources/hello.dmn");
        filter(source, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        filter(controlSource, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(25, TimeUnit.SECONDS).until(() -> getRestResponse("/control").contains("Ciao, v1"));

        System.out.println("Evaluate DMN");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"v1\"   " +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "hello")
                .then()
                .statusCode(200)
                .body("greeting", is("Ciao, v1"));

        // --- Change #2
        System.out.println("Beginning Change #2");
        filter(source, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        filter(controlSource, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(25, TimeUnit.SECONDS).until(() -> getRestResponse("/control").contains("Bonjour, v1"));

        System.out.println("Evaluate DMN");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"v1\"   " +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "hello")
                .then()
                .statusCode(200)
                .body("greeting", is("Bonjour, v1"));

        System.out.println("done.");
    }

    @Test
    public void testDRLHotReload() throws Exception {
        testDir = initProject("projects/classic-inst", "projects/project-intrumentation-reload-drl");
        run(true);

        final File controlSource = new File(testDir, "src/main/java/control/RestControl.java");

        // await Quarkus
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(1, TimeUnit.MINUTES).until(() -> getRestResponse("/control").contains("Hello, v1"));

        System.out.println("Evaluate DRL");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"strings\": [\"v1\"]  " +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "q1")
                .then()
                .statusCode(200)
                .body(containsString("Hello, v1"));

        // --- Change #1
        System.out.println("Beginning Change #1");
        File source = new File(testDir, "src/main/resources/acme/rules.drl");
        filter(source, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        filter(controlSource, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(25, TimeUnit.SECONDS).until(() -> getRestResponse("/control").contains("Ciao, v1"));

        System.out.println("Evaluate DRL");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"strings\": [\"v1\"]  " +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "q1")
                .then()
                .statusCode(200)
                .body(containsString("Ciao, v1"));

        // --- Change #2
        System.out.println("Beginning Change #2");
        filter(source, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        filter(controlSource, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        await().pollDelay(1, TimeUnit.SECONDS)
                .atMost(25, TimeUnit.SECONDS).until(() -> getRestResponse("/control").contains("Bonjour, v1"));

        System.out.println("Evaluate DRL");
        given().baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"strings\": [\"v1\"]  " +
                        "}")
                .contentType(ContentType.JSON)
                .when()
                .post("/" + "q1")
                .then()
                .statusCode(200)
                .body(containsString("Bonjour, v1"));

        System.out.println("done.");
    }
}

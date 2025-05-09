/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package control;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.kogito.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class DMNHotReloadTest {

    private static final long INIT_POLL_DELAY = 3;
    private static final TimeUnit INIT_POLL_DELAY_UNIT = SECONDS;
    private static final long INIT_POLL_TIMEOUT = 2;
    private static final TimeUnit INIT_POLL_TIMEOUT_UNIT = TimeUnit.MINUTES;
    private static final long RELOAD_POLL_DELAY = INIT_POLL_DELAY;
    private static final TimeUnit RELOAD_POLL_DELAY_UNIT = INIT_POLL_DELAY_UNIT;
    private static final long RELOAD_POLL_TIMEOUT = 30;
    private static final TimeUnit RELOAD_POLL_TIMEOUT_UNIT = SECONDS;

    private static final String DMN_HOT_RELOAD = "testDMNHotReload";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = LoggerFactory.getLogger(DMNRuntime.class);

    private static File testDir;
    private static File controlSource;
    private static File source;

    private static final int port = 8080;
    private static final String modelUrl = String.format("http://localhost:%s/hello", port);
    private static final String controlUrl = String.format("http://localhost:%s/control", port);

    @BeforeAll
    static void beforeAll() throws MavenInvocationException {
        LOGGER.info(String.format("Setup application: %s", DMN_HOT_RELOAD));
        testDir =new File(System.getProperty("user.dir"));
        LOGGER.info("testDir:"+testDir.getAbsolutePath());
        controlSource = new File(testDir, "src/main/java/control/RestControl.java");
        assertThat(controlSource.exists()).isTrue();
        LOGGER.info("controlSource " + controlSource.getAbsolutePath());
        source = new File(testDir, "src/main/resources/hello.dmn");
        assertThat(source.exists()).isTrue();
        LOGGER.info("source " + source.getAbsolutePath());
        LOGGER.info("port " + port);
        LOGGER.info("modelUrl " + modelUrl);
        LOGGER.info("controlUrl " + controlUrl);
        LOGGER.info(String.format("Starting application: %s", DMN_HOT_RELOAD));
        InvocationRequest request = new DefaultInvocationRequest();
        File pomFile = new File(testDir, "pom.xml");
        assertThat(pomFile.exists()).isTrue();
        request.setPomFile( pomFile);
        request.addArg("quarkus:dev");

        Invoker invoker = new DefaultInvoker();
        InvocationResult execute = invoker.execute(request);

    }


    @Test
    public void testDMNHotReload() throws Exception {


//        int port = getPort();
//        LOGGER.info("port " + port);
//        String modelUrl = String.format("http://localhost:%s/hello", port);
//        LOGGER.info("modelUrl " + modelUrl);
//        String controlUrl = String.format("http://localhost:%s/control", port);
//        LOGGER.info("controlUrl " + controlUrl);

        assertThat(getModelResponse(modelUrl)).isEqualTo("Hello, v1");

//        ValidatableResponse greeting = given()
//                .contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body("{\n" +
//                        "    \"name\": \"v1\"   " +
//                        "}")
//                .when()
//                .post("/" + "hello")
//                .then()
//                .statusCode(200)
//                .body("greeting", is("Hello, v1"));
//        LOGGER.info("greeting " + greeting);

        //--- Change #1
        modifyFiles(DMN_HOT_RELOAD,
                Map.of(source, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"),
                        controlSource, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+")),
                1);
        await().pollDelay(RELOAD_POLL_DELAY, RELOAD_POLL_DELAY_UNIT)
                .atMost(RELOAD_POLL_TIMEOUT, RELOAD_POLL_TIMEOUT_UNIT).until(() -> getRestControlResponse(controlUrl).contains("Ciao, v1"));
//
//        logEvaluate(DMN_HOT_RELOAD, "DMN");
//        given().contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body("{\n" +
//                        "    \"name\": \"v1\"   " +
//                        "}")
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/" + "hello")
//                .then()
//                .statusCode(200)
//                .body("greeting", is("Ciao, v1"));
//
//        // --- Change #2
//        modifyFiles(DMN_HOT_RELOAD,
//                Map.of(source, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"),
//                        controlSource, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+")),
//                2);
//        System.out.println(getRestControlResponse());
//        await().pollDelay(RELOAD_POLL_DELAY, RELOAD_POLL_DELAY_UNIT)
//                .atMost(RELOAD_POLL_TIMEOUT, RELOAD_POLL_TIMEOUT_UNIT).until(() -> getRestControlResponse().contains("Bonjour, v1"));
//
//        logEvaluate(DMN_HOT_RELOAD, "DMN");
//        given().contentType(ContentType.JSON)
//                .accept(ContentType.JSON)
//                .body("{\n" +
//                        "    \"name\": \"v1\"   " +
//                        "}")
//                .contentType(ContentType.JSON)
//                .when()
//                .post("/" + "hello")
//                .then()
//                .statusCode(200)
//                .body("greeting", is("Bonjour, v1"));
//
//        logDone(DMN_HOT_RELOAD);
    }

    private void logEvaluate(String test, String model) {
        LOGGER.info("[{}] Evaluate {}", test, model);
    }

    private void logChange(String test, int change) {
        LOGGER.info("[{}] Beginning Change #{}", test, change);
    }

    private void logDone(String test) {
        LOGGER.info("[{}] done.", test);
    }

    private int getPort() {
        LOGGER.info("getPort");
        RequestSpecification requestSpecification = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\n" +
                        "    \"name\": \"v1\"   " +
                        "}")
                .contentType(ContentType.JSON);
        return ((RequestSpecificationImpl) requestSpecification).getPort();
    }

    private String getModelResponse(String urlStr) throws Exception {
        LOGGER.info("[{}] getModelResponse", DMN_HOT_RELOAD);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlStr))
                .version(HttpClient.Version.HTTP_2)
                .timeout(Duration.of(10, ChronoUnit.SECONDS))
                .headers("Content-Type", ContentType.JSON.toString())
                .headers("accept", ContentType.JSON.toString())
                .POST(HttpRequest.BodyPublishers.ofString("{\n" +
                        "    \"name\": \"v1\"   " +
                        "}"))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonNode = MAPPER.readTree(response.body());
        return jsonNode.get("greeting").textValue();
    }

    private String getRestControlResponse(String urlStr) {
        LOGGER.info("[{}] getRestControlResponse", DMN_HOT_RELOAD);
//        try {
//            ResponseBody body = given().contentType(ContentType.JSON)
//                    .accept(ContentType.TEXT)
//                    .when()
//                    .get("/" + "control")
//                    .body();
//            return body.print();
//        } catch (Exception e) {
//            return "";
//        }

        AtomicReference<String> resp = new AtomicReference<>();
        // retry on exceptions for connection refused, connection errors, etc. which will occur until the Kogito Quarkus maven project is fully built and running
        await().pollDelay(INIT_POLL_DELAY, INIT_POLL_DELAY_UNIT)
                .atMost(INIT_POLL_TIMEOUT, INIT_POLL_TIMEOUT_UNIT).until(() -> {
                    LOGGER.info("[{}] getRestControlResponse", DMN_HOT_RELOAD);
                    try {
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(new URI(urlStr))
                                .version(HttpClient.Version.HTTP_2)
                                .timeout(Duration.of(10, ChronoUnit.SECONDS))
                                .GET()
                                .build();
                        HttpClient client = HttpClient.newHttpClient();
                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        resp.set(response.body());
//                        ResponseBody body = given().contentType(ContentType.JSON)
//                                .accept(ContentType.TEXT)
//                                .when()
//                                .get("/" + "control")
//                                .body();
//                        String content = body.print();
//                        String urlStr = String.format("http://localhost:%s/control", port);
//                        String content = IOUtils.toString(new URL(urlStr), StandardCharsets.UTF_8);
//                        resp.set(content);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
        return resp.get();
    }

    /**
     * Method used to modify the given files with the associated modification map
     *
     * @param test
     * @param mappedModifications
     * @throws IOException
     */
    private void modifyFiles(String test, Map<File, Map<String, String>> mappedModifications, int change) throws IOException {
        logChange(test, change);
        for (Map.Entry<File, Map<String, String>> entry : mappedModifications.entrySet()) {
            LOGGER.info("[{}] Modifying file {}", test, entry.getKey());
            filter(entry.getKey(), entry.getValue());
        }
    }

    private void filter(File input, Map<String, String> variables) throws IOException {
        assertThat(input).isFile();
        String data = FileUtils.readFileToString(input, "UTF-8");
        for (Map.Entry<String, String> token : variables.entrySet()) {
            String value = String.valueOf(token.getValue());
            data = data.replace(token.getKey(), value);
        }
        FileUtils.write(input, data, "UTF-8");
    }
}   
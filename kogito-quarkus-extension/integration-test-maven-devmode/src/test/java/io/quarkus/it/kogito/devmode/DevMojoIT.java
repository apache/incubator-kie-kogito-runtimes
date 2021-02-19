package io.quarkus.it.kogito.devmode;

import java.io.File;
import java.util.Collections;

import io.quarkus.maven.it.RunAndCheckMojoTestBase;
import io.quarkus.test.devmode.util.DevModeTestUtils;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

//@DisableForNative ??
public class DevMojoIT extends RunAndCheckMojoTestBase {

    private static final String HTTP_TEST_PORT = "8080";

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    // TODO @Test
    public void testBPMN2HotReload() throws Exception {
        testDir = initProject("projects/classic-inst", "projects/project-intrumentation-reload-bpmn");
        run(true);
        
        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Starting bpmn process");
        given()
                .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
        Thread.sleep(1_000);
        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Starting bpmn process");
        given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
        Thread.sleep(1_000);
        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Starting bpmn process");
        given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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

    // TODO @Test
    public void testDMNHotReload() throws Exception {
        testDir = initProject("projects/classic-inst", "projects/project-intrumentation-reload-dmn");
        run(true);

        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Evaluate DMN");
        given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
        Thread.sleep(1_000);
        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Evaluate DMN");
        given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
        Thread.sleep(1_000);
        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Evaluate DMN");
        given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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

        DevModeTestUtils.getHttpResponse(); // await Quarkus 

        System.out.println("Evaluate DRL");
        String response = given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
               .extract().asString();
        System.out.println(response);
        
        // --- Change #1
        System.out.println("Beginning Change #1");
        File source = new File(testDir, "src/main/resources/acme/rules.drl");
        filter(source, Collections.singletonMap("\"Hello, \"+", "\"Ciao, \"+"));
        Thread.sleep(1_000);
        DevModeTestUtils.getHttpResponse(); // await Quarkus 
        System.out.println("Evaluate DRL");
        response = given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
               .extract().asString();
        System.out.println(response);
        
        // --- Change #2
        System.out.println("Beginning Change #2");
        filter(source, Collections.singletonMap("\"Ciao, \"+", "\"Bonjour, \"+"));
        Thread.sleep(1_000);
        DevModeTestUtils.getHttpResponse(); // await Quarkus 
        System.out.println("Evaluate DRL");
        response = given()
               .baseUri("http://localhost:" + HTTP_TEST_PORT)
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
               .extract().asString();
        System.out.println(response);

        System.out.println("done.");
    }
}
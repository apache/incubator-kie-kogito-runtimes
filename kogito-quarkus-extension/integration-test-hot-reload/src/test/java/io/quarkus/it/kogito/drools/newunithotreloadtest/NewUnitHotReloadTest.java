package io.quarkus.it.kogito.drools.newunithotreloadtest;

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


public class NewUnitHotReloadTest {

    private static final String PACKAGE = "io.quarkus.it.kogito.drools.newunithotreloadtest";
    private static final String RESOURCE_FILE_PATH = PACKAGE.replace( '.', '/' );
    private static final String DRL_RESOURCE_FILE = RESOURCE_FILE_PATH + "/PersonUnit.drl";
    
    private static final String HTTP_TEST_PORT = "65535";

    @RegisterExtension
    final static QuarkusDevModeTest test = new QuarkusDevModeTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class));

    @Test
    public void testServletChange() throws InterruptedException {

        String personsPayload = "{\"persons\":[{\"name\":\"Mario\",\"age\":45,\"adult\":false},{\"name\":\"Sofia\",\"age\":17,\"adult\":false}]}";

        test.addSourceFile(Person.class);
        test.addSourceFile(PersonUnit.class);
        test.addResourceFile("PersonUnit.drl.txt", DRL_RESOURCE_FILE);
        
        List<String> names = given()
                .baseUri("http://localhost:" + HTTP_TEST_PORT)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(personsPayload).when()
                .post("/find-adult-names")
                .then()
                .statusCode(200)
                .extract().
                        as(List.class);

        assertEquals(1, names.size());
        assertTrue(names.contains( "Mario" ));
    }
}
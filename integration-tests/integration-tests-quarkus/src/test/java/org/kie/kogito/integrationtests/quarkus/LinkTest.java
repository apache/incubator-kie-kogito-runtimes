package org.kie.kogito.integrationtests.quarkus;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.http.ContentType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;

public class LinkTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create( JavaArchive.class)
                    .addAsResource("SimpleLinkTest.bpmn.test", "src/main/resources/SimpleLinkTest.bpmn"));

    @Test
    void testLink() {
        given()
                .contentType(ContentType.JSON)
            .when()
                .post("/SimpleLinkTest")
            .then()
                .statusCode(201);
    }

}

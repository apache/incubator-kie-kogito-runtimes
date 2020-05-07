package ${package};

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingsTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/greetings")
          .then()
             .statusCode(200)
             .body(is("hello"));
    }

}
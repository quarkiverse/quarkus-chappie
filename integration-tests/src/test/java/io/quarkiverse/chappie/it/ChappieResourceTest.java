package io.quarkiverse.chappie.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ChappieResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/chappie")
                .then()
                .statusCode(200)
                .body(is("Hello chappie"));
    }
}

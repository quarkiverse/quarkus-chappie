package io.quarkiverse.chappie.sample;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloWithoutName() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.TEXT_PLAIN)
                .body(is("Hello"));
    }

    @Test
    public void testHelloWithName() {
        given()
                .queryParam("name", "John")
                .when().get("/hello")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(MediaType.TEXT_PLAIN)
                .body(is("Hello John"));
    }
}

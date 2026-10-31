package com.example.ecommerce;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApiIntegrationTest {
    @LocalServerPort int port;

    // Requires the Oracle database/schema configured in application.properties to be available.
    @Test
    void getProductsReturnsJsonArray() {
        given().baseUri("http://localhost").port(port)
            .when().get("/api/products")
            .then().statusCode(200).contentType(containsString("application/json"));
    }
}

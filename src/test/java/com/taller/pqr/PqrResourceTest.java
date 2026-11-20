package com.taller.pqr;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/** Pruebas de la API levantando Quarkus. */
//@Disabled
@QuarkusTest
class PqrResourceTest {

    @Test
    void consultaPqrValida() {
        given()
                .when().get("/pqr/PQR-2026")
                .then()
                .statusCode(200)
                .body("id", equalTo("PQR-2026"))
                .body("mensaje", containsString("PQR-2026"));
    }

    @Test
    void rechazaIdInvalido() {
        given()
                .when().get("/pqr/<script>")
                .then()
                .statusCode(400);
    }

    @Test
    void healthDisponible() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200);
    }
}

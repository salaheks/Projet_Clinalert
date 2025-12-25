package com.clinalert.doctortracker.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests for Authentication endpoints
 * Tests: 6 scenarios (3 positive + 3 negative)
 * FIXED: Response structure is flat (email, role, token) not nested
 * (user.email)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    @DisplayName("AUTH_001: Login Doctor with valid credentials")
    void testLoginDoctorSuccess() {
        String loginRequest = """
                {
                    "email": "house@clinalert.com",
                    "password": "doctor123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("house@clinalert.com"))
                .body("role", equalTo("DOCTOR"));
    }

    @Test
    @DisplayName("AUTH_002: Login Patient with valid credentials")
    void testLoginPatientSuccess() {
        String loginRequest = """
                {
                    "email": "john.doe@clinalert.com",
                    "password": "patient123"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("john.doe@clinalert.com"))
                .body("role", equalTo("PATIENT"));
    }

    @Test
    @DisplayName("AUTH_004_NEG: Login with invalid credentials")
    void testLoginInvalidCredentials() {
        String loginRequest = """
                {
                    "email": "house@clinalert.com",
                    "password": "WRONG_PASSWORD"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    @DisplayName("AUTH_005_NEG: Password too short")
    void testLoginPasswordTooShort() {
        String loginRequest = """
                {
                    "email": "house@clinalert.com",
                    "password": "1234"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401)));
    }

    @Test
    @DisplayName("AUTH_006_NEG: Empty credentials")
    void testLoginEmptyFields() {
        String loginRequest = """
                {
                    "email": "",
                    "password": ""
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(anyOf(is(400), is(401)));
    }
}

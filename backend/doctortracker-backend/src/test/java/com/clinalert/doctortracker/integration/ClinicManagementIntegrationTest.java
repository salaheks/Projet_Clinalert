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
 * Integration Tests for Clinic Management endpoints
 * Tests: 4 scenarios (CRUD operations)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Clinic Management Integration Tests")
public class ClinicManagementIntegrationTest {

    @LocalServerPort
    private int port;

    private String doctorToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Login as doctor
        String loginRequest = """
                {
                    "email": "house@clinalert.com",
                    "password": "doctor123"
                }
                """;

        doctorToken = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/auth/login")
                .then()
                .extract()
                .path("token");
    }

    @Test
    @DisplayName("CRUD_CLI_001: CREATE Clinic with valid data")
    void testCreateClinic() {
        // GIVEN - Valid clinic data
        String clinicRequest = """
                {
                    "name": "Integration Test Clinic",
                    "address": "123 Test Street",
                    "phone": "0123456789"
                }
                """;

        // WHEN - POST /api/clinics
        given()
                .header("Authorization", "Bearer " + doctorToken)
                .contentType(ContentType.JSON)
                .body(clinicRequest)
                .when()
                .post("/api/clinics")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("name", equalTo("Integration Test Clinic"))
                .body("address", equalTo("123 Test Street"))
                .body("phone", equalTo("0123456789"));
    }

    @Test
    @DisplayName("CRUD_CLI_002: READ List of Clinics")
    void testReadClinicsList() {
        // WHEN - GET /api/clinics
        given()
                .header("Authorization", "Bearer " + doctorToken)
                .when()
                .get("/api/clinics")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @DisplayName("CRUD_CLI_003: UPDATE Clinic (To implement when UI ready)")
    void testUpdateClinic() {
        // TODO: Implement when UPDATE endpoint is available
    }

    @Test
    @DisplayName("CRUD_CLI_004: DELETE Clinic (To implement when UI ready)")
    void testDeleteClinic() {
        // TODO: Implement when DELETE endpoint is available
    }
}

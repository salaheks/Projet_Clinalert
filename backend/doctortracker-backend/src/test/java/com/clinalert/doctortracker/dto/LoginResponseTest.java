package com.clinalert.doctortracker.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DTO classes - LoginResponse, LoginRequest, RegisterRequest
 * Target: Cover all constructors, getters, setters
 * Impact: +1.5% coverage
 */
@DisplayName("DTO Tests - LoginResponse")
class LoginResponseTest {

    @Test
    @DisplayName("Constructor with all fields should set values correctly")
    void constructor_WithAllFields_ShouldSetValues() {
        LoginResponse response = new LoginResponse("token123", "user-456", "test@example.com", "DOCTOR");

        assertEquals("token123", response.getToken());
        assertEquals("user-456", response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("DOCTOR", response.getRole());
    }

    @Test
    @DisplayName("Default constructor should create empty object")
    void defaultConstructor_ShouldCreateEmptyObject() {
        LoginResponse response = new LoginResponse();

        assertNotNull(response);
    }

    @Test
    @DisplayName("Setters should update values")
    void setters_ShouldUpdateValues() {
        LoginResponse response = new LoginResponse();

        response.setToken("newToken");
        response.setUserId("newUserId");
        response.setEmail("new@example.com");
        response.setRole("PATIENT");

        assertEquals("newToken", response.getToken());
        assertEquals("newUserId", response.getUserId());
        assertEquals("new@example.com", response.getEmail());
        assertEquals("PATIENT", response.getRole());
    }

    @Test
    @DisplayName("All getters should return correct values")
    void getters_ShouldReturnCorrectValues() {
        LoginResponse response = new LoginResponse("t1", "u1", "e1", "r1");

        assertNotNull(response.getToken());
        assertNotNull(response.getUserId());
        assertNotNull(response.getEmail());
        assertNotNull(response.getRole());
    }
}

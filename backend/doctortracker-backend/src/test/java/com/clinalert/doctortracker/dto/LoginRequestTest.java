package com.clinalert.doctortracker.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional DTO tests for LoginRequest
 */
@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {

    @Test
    @DisplayName("Constructor and getters should work")
    void constructorAndGetters_ShouldWork() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    @DisplayName("Empty values should be allowed")
    void emptyValues_ShouldBeAllowed() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("");

        assertEquals("", request.getEmail());
        assertEquals("", request.getPassword());
    }

    @Test
    @DisplayName("Null values should be allowed")
    void nullValues_ShouldBeAllowed() {
        LoginRequest request = new LoginRequest();
        request.setEmail(null);
        request.setPassword(null);

        assertNull(request.getEmail());
        assertNull(request.getPassword());
    }
}

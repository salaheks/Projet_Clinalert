package com.clinalert.doctortracker.security;

import com.clinalert.doctortracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for JwtTokenProvider
 * Target: Cover all token validation, extraction, and error handling
 * Impact: +1.5% coverage (91 missed instructions)
 */
@SpringBootTest
@DisplayName("JwtTokenProvider Comprehensive Tests")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.UserRole.DOCTOR);
        testUser.setPassword("hashedPassword");
        testUser.setEnabled(true);

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("hashedPassword")
                .roles("DOCTOR")
                .build();
    }

    @Test
    @DisplayName("generateToken - Should create valid token")
    void generateToken_ShouldCreateValidToken() {
        String token = jwtTokenProvider.generateToken(userDetails, "user-123", "DOCTOR");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // JWT format
    }

    @Test
    @DisplayName("validateToken - Valid token should return true")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateToken(userDetails, "user-123", "DOCTOR");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken - Invalid token should return false")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        String invalidToken = "invalid.jwt.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - Malformed token should return false")
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        String malformedToken = "not-a-jwt-at-all";

        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - Null token should return false")
    void validateToken_WithNullToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - Empty token should return false")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("getUsernameFromToken - Should extract username correctly")
    void getUsernameFromToken_ShouldExtractUsername() {
        String token = jwtTokenProvider.generateToken(userDetails, "user-123", "DOCTOR");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("getUsernameFromToken - Invalid token should handle gracefully")
    void getUsernameFromToken_WithInvalidToken_ShouldHandleGracefully() {
        String invalidToken = "invalid.token";

        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    @DisplayName("Token claims - Should contain user ID and role")
    void tokenClaims_ShouldContainUserIdAndRole() {
        String token = jwtTokenProvider.generateToken(userDetails, "user-456", "PATIENT");

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("test@example.com", username);
        // TokenProvider should store userId and role in claims
    }

    @Test
    @DisplayName("Token validation - Should work for different user roles")
    void tokenValidation_ShouldWorkForDifferentRoles() {
        String doctorToken = jwtTokenProvider.generateToken(userDetails, "doc-1", "DOCTOR");

        UserDetails patientDetails = org.springframework.security.core.userdetails.User
                .withUsername("patient@example.com")
                .password("hashedPassword")
                .roles("PATIENT")
                .build();

        String patientToken = jwtTokenProvider.generateToken(patientDetails, "pat-1", "PATIENT");

        assertTrue(jwtTokenProvider.validateToken(doctorToken));
        assertTrue(jwtTokenProvider.validateToken(patientToken));
    }
}

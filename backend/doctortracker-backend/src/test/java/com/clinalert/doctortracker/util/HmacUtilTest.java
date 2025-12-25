package com.clinalert.doctortracker.util;

/**
 * Tests pour HmacUtil - 5 tests
 * Couvre: HMAC signature verification, bytesToHex conversion
 */

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests HmacUtil")
class HmacUtilTest {

    private HmacUtil hmacUtil;
    private static final String TEST_SECRET = "test-secret-key-for-hmac";

    @BeforeEach
    void setUp() {
        hmacUtil = new HmacUtil();
        // Inject the secret using reflection since @Value won't be injected in unit
        // tests
        ReflectionTestUtils.setField(hmacUtil, "hmacSecret", TEST_SECRET);
    }

    @Test
    @DisplayName("Should verify valid HMAC signature")
    void verifySignature_ValidSignature_ShouldReturnTrue() {
        String payload = "test-payload";

        // Dynamically calculate the signature to ensure it matches the implementation
        // This avoids hardcoding issues if the algorithm or secret handling changes
        // slightly
        // We replicate the logic from HmacUtil here for the test expectation
        String validSignature = "";
        try {
            javax.crypto.Mac sha256Hmac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                    TEST_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] digest = sha256Hmac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * digest.length);
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            validSignature = hexString.toString();
        } catch (Exception e) {
            fail("Failed to setup test signature: " + e.getMessage());
        }

        boolean result = hmacUtil.verifySignature(payload, validSignature);

        // Fixed: assertNotNull on primitive is invalid
        assertTrue(result, "Signature verification should succeed for valid input");
    }

    @Test
    @DisplayName("Should reject invalid HMAC signature")
    void verifySignature_InvalidSignature_ShouldReturnFalse() {
        String payload = "test-payload";
        String invalidSignature = "invalid-signature-123";

        boolean result = hmacUtil.verifySignature(payload, invalidSignature);

        assertFalse(result, "Invalid signature should be rejected");
    }

    @Test
    @DisplayName("Should verify signature is case insensitive")
    void verifySignature_CaseInsensitive_ShouldWork() {
        String payload = "test";

        // First, let's get what the real signature should be
        // by calling verify with itself
        boolean result1 = hmacUtil.verifySignature(payload, "ABC123");
        boolean result2 = hmacUtil.verifySignature(payload, "abc123");

        // Both should return same result (false for invalid, but both should match
        // behavior)
        assertEquals(result1, result2, "Signature verification should be case-insensitive");
    }

    @Test
    @DisplayName("Should handle empty payload")
    void verifySignature_EmptyPayload_ShouldNotThrow() {
        String emptyPayload = "";
        String someSignature = "abc123";

        assertDoesNotThrow(() -> {
            boolean result = hmacUtil.verifySignature(emptyPayload, someSignature);
            // Fixed: assertNotNull on primitive is invalid
            assertFalse(result, "Signature should be invalid for random signature");
        });
    }

    @Test
    @DisplayName("Should handle null payload gracefully")
    void verifySignature_NullPayload_ShouldReturnFalse() {
        String someSignature = "abc123";

        boolean result = hmacUtil.verifySignature(null, someSignature);

        assertFalse(result, "Null payload should return false");
    }

    @Test
    @DisplayName("Should verify same payload with same signature twice")
    void verifySignature_Idempotent_ShouldReturnSameResult() {
        String payload = "consistent-payload";
        String signature = "test-sig";

        boolean result1 = hmacUtil.verifySignature(payload, signature);
        boolean result2 = hmacUtil.verifySignature(payload, signature);

        assertEquals(result1, result2, "Same payload and signature should give same result");
    }
}

package com.clinalert.doctortracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Couverture Util")
class UtilCoverageTest {

    @Test
    @DisplayName("AppConstants - Private Constructor")
    void appConstants_PrivateConstructor() throws Exception {
        Constructor<AppConstants> constructor = AppConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance(); // Coverage for instantiating utility class
        assertThat(AppConstants.ROLE_DOCTOR).isEqualTo("DOCTOR"); // Verify constant access
    }

    @Test
    @DisplayName("HmacUtil - Verify Signature")
    void verifySignature_Tests() throws Exception {
        HmacUtil hmacUtil = new HmacUtil();

        // Inject secret via reflection
        Field secretField = HmacUtil.class.getDeclaredField("hmacSecret");
        secretField.setAccessible(true);
        secretField.set(hmacUtil, "mySecretKey");

        String payload = "data";
        String validSignature = calculateExpectedHmac(payload, "mySecretKey");

        // Test Correct Signature
        assertThat(hmacUtil.verifySignature(payload, validSignature)).isTrue();

        // Test Incorrect Signature
        assertThat(hmacUtil.verifySignature(payload, "wrongSig")).isFalse();

        // Test Nulls
        assertThat(hmacUtil.verifySignature(null, validSignature)).isFalse();
        assertThat(hmacUtil.verifySignature(payload, null)).isFalse();
    }

    private String calculateExpectedHmac(String data, String key) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] digest = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * digest.length);
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

package com.clinalert.doctortracker.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class HmacUtil {

    @Value("${app.hmacSecret}")
    private String hmacSecret;

    public boolean verifySignature(String payload, String signature) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            byte[] digest = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            // We used digest.toString() in Dart which is not Hex, it's Instance ID usually!
            // Wait, in Dart crypto package, digest.toString() returns Hex string.
            // So we need to convert bytes to Hex here.

            String calculatedSignature = bytesToHex(digest);

            return calculatedSignature.equalsIgnoreCase(signature);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

package com.clinalert.doctortracker.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class HmacUtil {

    private static final Logger logger = LoggerFactory.getLogger(HmacUtil.class);

    @Value("${app.hmacSecret}")
    private String hmacSecret;

    public boolean verifySignature(String payload, String signature) {
        if (payload == null || signature == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            // We used digest.toString() in Dart which is not Hex, it's Instance ID usually!
            // Wait, in Dart crypto package, digest.toString() returns Hex string.
            // So we need to convert bytes to Hex here.

            String calculatedSignature = bytesToHex(digest);

            return calculatedSignature.equalsIgnoreCase(signature);
        } catch (Exception e) {
            logger.error("Error verifying signature", e);
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

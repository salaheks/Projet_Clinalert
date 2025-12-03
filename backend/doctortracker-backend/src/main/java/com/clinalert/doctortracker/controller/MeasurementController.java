package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.service.MeasurementService;
import com.clinalert.doctortracker.util.HmacUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    @Autowired
    private MeasurementService measurementService;

    @Autowired
    private HmacUtil hmacUtil;

    @PostMapping
    public ResponseEntity<?> receiveMeasurements(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String rawBody) {
        // 1. Check HMAC if present
        if (signature != null) {
            if (!hmacUtil.verifySignature(rawBody, signature)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid HMAC Signature");
            }
        }
        // 2. Check JWT if HMAC is missing
        else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // In a real app, Spring Security filter chain handles this.
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authentication");
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            List<Measurement> measurements = mapper.readValue(rawBody,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Measurement>>() {
                    });

            // Save measurements using service
            if (measurements != null) {
                measurementService.saveMeasurements(measurements);
                return ResponseEntity.ok().body("Measurements received: " + measurements.size());
            } else {
                return ResponseEntity.badRequest().body("No measurements provided");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid JSON: " + e.getMessage());
        }
    }

    @GetMapping("/{patientId}")
    public List<Measurement> getHistory(@PathVariable String patientId) {
        return measurementService.getHistory(patientId);
    }
}

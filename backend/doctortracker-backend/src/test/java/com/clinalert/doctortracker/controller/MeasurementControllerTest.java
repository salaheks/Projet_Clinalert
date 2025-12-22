package com.clinalert.doctortracker.controller;

/**
 * Tests MeasurementController - Integration - 5 tests
 * MeasurementController uses custom HMAC signature auth, not Spring Security!
 */

import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.service.MeasurementService;
import com.clinalert.doctortracker.util.HmacUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests Controller Measurement - Integration")
class MeasurementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MeasurementService measurementService;

    @MockBean
    private HmacUtil hmacUtil;

    private Measurement measurement;

    @BeforeEach
    void setUp() {
        measurement = new Measurement();
        measurement.setId("meas-001");
        measurement.setPatientId("patient-001");
        measurement.setType("Heart Rate");
        measurement.setValue(75.0);
        measurement.setTimestamp(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/measurements - With valid HMAC signature")
    void saveMeasurements_WithValidSignature_ShouldSave() throws Exception {
        when(hmacUtil.verifySignature(anyString(), eq("valid-signature"))).thenReturn(true);
        when(measurementService.saveMeasurements(anyList())).thenReturn(Arrays.asList(measurement));

        String jsonBody = "[" + objectMapper.writeValueAsString(measurement) + "]";

        mockMvc.perform(post("/api/measurements")
                .header("X-Signature", "valid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Measurements received: 1")));

        verify(hmacUtil).verifySignature(anyString(), eq("valid-signature"));
        verify(measurementService).saveMeasurements(anyList());
    }

    @Test
    @DisplayName("POST /api/measurements - With Bearer token")
    void saveMeasurements_WithBearerToken_ShouldSave() throws Exception {
        when(measurementService.saveMeasurements(anyList())).thenReturn(Arrays.asList(measurement));

        String jsonBody = "[" + objectMapper.writeValueAsString(measurement) + "]";

        mockMvc.perform(post("/api/measurements")
                .header("Authorization", "Bearer fake-jwt-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Measurements received: 1")));
    }

    @Test
    @DisplayName("POST /api/measurements - Missing authentication")
    void saveMeasurements_NoAuth_ShouldReturn401() throws Exception {
        String jsonBody = "[" + objectMapper.writeValueAsString(measurement) + "]";

        mockMvc.perform(post("/api/measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Missing Authentication"));
    }

    @Test
    @DisplayName("POST /api/measurements - Invalid HMAC signature")
    void saveMeasurements_InvalidSignature_ShouldReturn403() throws Exception {
        when(hmacUtil.verifySignature(anyString(), anyString())).thenReturn(false);

        String jsonBody = "[" + objectMapper.writeValueAsString(measurement) + "]";

        mockMvc.perform(post("/api/measurements")
                .header("X-Signature", "invalid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Invalid HMAC Signature"));
    }

    @Test
    @DisplayName("GET /api/measurements/{patientId} - Should return measurements")
    void getHistory_ShouldReturnList() throws Exception {
        when(measurementService.getHistory("patient-001")).thenReturn(Arrays.asList(measurement));

        // GET endpoint doesn't require authentication
        mockMvc.perform(get("/api/measurements/patient-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].patientId").value("patient-001"));

        verify(measurementService).getHistory("patient-001");
    }
}

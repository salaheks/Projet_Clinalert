package com.clinalert.doctortracker.controller;

/**
 * Tests SmartWatchController - Integration - 15 tests
 * Couvre: Device endpoints, health data, daily summaries
 */

import com.clinalert.doctortracker.model.DailyHealthSummary;
import com.clinalert.doctortracker.model.HealthData;
import com.clinalert.doctortracker.model.SmartWatchDevice;
import com.clinalert.doctortracker.service.SmartWatchHealthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests SmartWatch Controller - Integration")
class SmartWatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SmartWatchHealthService smartWatchHealthService;

    private SmartWatchDevice device;
    private HealthData healthData;

    @BeforeEach
    void setUp() {
        device = new SmartWatchDevice();
        device.setId("device-001");
        device.setPatientId("patient-001");
        device.setDeviceAddress("AA:BB:CC:DD:EE:FF");
        device.setIsActive(true);

        healthData = new HealthData();
        healthData.setId("health-001");
        healthData.setPatientId("patient-001");
        healthData.setHeartRate(75);
        healthData.setTimestamp(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/smartwatch/devices")
    void registerDevice_ShouldCreate() throws Exception {
        when(smartWatchHealthService.registerDevice(any())).thenReturn(device);

        mockMvc.perform(post("/api/smartwatch/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(device)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/devices/{patientId}")
    void getPatientDevices_ShouldReturnList() throws Exception {
        when(smartWatchHealthService.getPatientDevices("patient-001")).thenReturn(Arrays.asList(device));

        mockMvc.perform(get("/api/smartwatch/devices/patient-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("device-001"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DELETE /api/smartwatch/devices/{deviceId}")
    void deleteDevice_ShouldDelete() throws Exception {
        doNothing().when(smartWatchHealthService).deleteDevice("device-001");

        mockMvc.perform(delete("/api/smartwatch/devices/device-001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/smartwatch/health-data")
    void submitHealthData_ShouldSave() throws Exception {
        when(smartWatchHealthService.saveHealthData(anyList())).thenReturn(Arrays.asList(healthData));

        mockMvc.perform(post("/api/smartwatch/health-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[" + objectMapper.writeValueAsString(healthData) + "]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/smartwatch/health-data - Empty list")
    void submitHealthData_EmptyList_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/smartwatch/health-data")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/health-data/{patientId}")
    void getPatientHealthData_ShouldReturnData() throws Exception {
        when(smartWatchHealthService.getPatientHealthData("patient-001")).thenReturn(Arrays.asList(healthData));

        mockMvc.perform(get("/api/smartwatch/health-data/patient-001"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/health-data/{patientId}/heart-rate")
    void getHeartRateHistory_ShouldReturnData() throws Exception {
        when(smartWatchHealthService.getHeartRateHistory("patient-001")).thenReturn(Arrays.asList(healthData));

        mockMvc.perform(get("/api/smartwatch/health-data/patient-001/heart-rate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/health-data/{patientId}/steps")
    void getStepsHistory_ShouldReturnData() throws Exception {
        when(smartWatchHealthService.getStepsHistory("patient-001")).thenReturn(Arrays.asList(healthData));

        mockMvc.perform(get("/api/smartwatch/health-data/patient-001/steps"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/smartwatch/daily-summary/{patientId}/generate")
    void generateDailySummary_ShouldGenerate() throws Exception {
        DailyHealthSummary summary = new DailyHealthSummary();
        summary.setPatientId("patient-001");
        when(smartWatchHealthService.generateDailySummary(eq("patient-001"), any())).thenReturn(summary);

        mockMvc.perform(post("/api/smartwatch/daily-summary/patient-001/generate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/daily-summary/{patientId}")
    void getRecentDailySummaries_ShouldReturnList() throws Exception {
        DailyHealthSummary summary = new DailyHealthSummary();
        when(smartWatchHealthService.getRecentDailySummaries("patient-001")).thenReturn(Arrays.asList(summary));

        mockMvc.perform(get("/api/smartwatch/daily-summary/patient-001"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/devices/{patientId}/active - Should return active devices")
    void getActiveDevices_ShouldReturnActiveOnly() throws Exception {
        when(smartWatchHealthService.getActiveDevices("patient-001")).thenReturn(Arrays.asList(device));

        mockMvc.perform(get("/api/smartwatch/devices/patient-001/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/smartwatch/devices/{deviceId}/deactivate")
    void deactivateDevice_ShouldUpdateStatus() throws Exception {
        doNothing().when(smartWatchHealthService).deactivateDevice("device-001");

        mockMvc.perform(put("/api/smartwatch/devices/device-001/deactivate"))
                .andExpect(status().isOk());

        verify(smartWatchHealthService).deactivateDevice("device-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/smartwatch/devices/{deviceId}/ping")
    void pingDevice_ShouldUpdateLastConnected() throws Exception {
        doNothing().when(smartWatchHealthService).updateDeviceLastConnected("device-001");

        mockMvc.perform(put("/api/smartwatch/devices/device-001/ping"))
                .andExpect(status().isOk());

        verify(smartWatchHealthService).updateDeviceLastConnected("device-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/smartwatch/health-data/single")
    void submitSingleHealthData_ShouldSave() throws Exception {
        when(smartWatchHealthService.saveHealthData(any(HealthData.class))).thenReturn(healthData);

        mockMvc.perform(post("/api/smartwatch/health-data/single")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(healthData)))
                .andExpect(status().isCreated());

        verify(smartWatchHealthService).saveHealthData(any(HealthData.class));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/health-data/{patientId}/spo2")
    void getSpO2History_ShouldReturnData() throws Exception {
        when(smartWatchHealthService.getSpO2History("patient-001")).thenReturn(Arrays.asList(healthData));

        mockMvc.perform(get("/api/smartwatch/health-data/patient-001/spo2"))
                .andExpect(status().isOk());

        verify(smartWatchHealthService).getSpO2History("patient-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/smartwatch/health-data/{patientId}/sleep")
    void getSleepHistory_ShouldReturnData() throws Exception {
        when(smartWatchHealthService.getSleepHistory("patient-001")).thenReturn(Arrays.asList(healthData));

        mockMvc.perform(get("/api/smartwatch/health-data/patient-001/sleep"))
                .andExpect(status().isOk());

        verify(smartWatchHealthService).getSleepHistory("patient-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/smartwatch/daily-summary/{patientId}/generate - No data available")
    void generateDailySummary_NoDataAvailable_ShouldReturnMessage() throws Exception {
        when(smartWatchHealthService.generateDailySummary(eq("patient-001"), any())).thenReturn(null);

        mockMvc.perform(post("/api/smartwatch/daily-summary/patient-001/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}

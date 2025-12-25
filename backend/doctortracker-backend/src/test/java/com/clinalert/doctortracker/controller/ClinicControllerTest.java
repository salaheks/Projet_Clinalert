package com.clinalert.doctortracker.controller;

/**
 * Tests ClinicController - Integration - 8 tests
 * Couvre: CRUD clinics, filter by doctor
 */

import com.clinalert.doctortracker.model.Clinic;
import com.clinalert.doctortracker.service.ClinicService;
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

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests Controller Clinic - Integration")
class ClinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClinicService clinicService;

    private Clinic clinic;

    @BeforeEach
    void setUp() {
        clinic = new Clinic();
        clinic.setId("clinic-001");
        clinic.setName("Test Clinic");
        clinic.setAddress("123 Test St");
        clinic.setPhone("0123456789");
        clinic.setDoctorId("doctor-001");
    }

    @Test
    @DisplayName("GET /api/clinics")
    void getAllClinics_ShouldReturnList() throws Exception {
        when(clinicService.getAllClinics()).thenReturn(Arrays.asList(clinic));

        mockMvc.perform(get("/api/clinics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Clinic"));
    }

    @Test
    @DisplayName("GET /api/clinics/{id}")
    void getClinicById_ShouldReturn() throws Exception {
        when(clinicService.getClinicById("clinic-001")).thenReturn(Optional.of(clinic));

        mockMvc.perform(get("/api/clinics/clinic-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Clinic"));
    }

    @Test
    @DisplayName("GET /api/clinics/{id} - Not found")
    void getClinicById_NotFound_ShouldReturn404() throws Exception {
        when(clinicService.getClinicById("clinic-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clinics/clinic-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/clinics/doctor/{id}")
    void getClinicsByDoctor_ShouldReturnList() throws Exception {
        when(clinicService.getClinicsByDoctorId("doctor-001")).thenReturn(Arrays.asList(clinic));

        mockMvc.perform(get("/api/clinics/doctor/doctor-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].doctorId").value("doctor-001"));
    }

    @Test
    @DisplayName("POST /api/clinics")
    void createClinic_ShouldCreate() throws Exception {
        when(clinicService.createClinic(any())).thenReturn(clinic);

        mockMvc.perform(post("/api/clinics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinic)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/clinics/{id}")
    void updateClinic_ShouldUpdate() throws Exception {
        when(clinicService.updateClinic(eq("clinic-001"), any())).thenReturn(clinic);

        mockMvc.perform(put("/api/clinics/clinic-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinic)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/clinics/{id} - Not found")
    void updateClinic_NotFound_ShouldReturn404() throws Exception {
        when(clinicService.updateClinic(eq("clinic-999"), any()))
                .thenThrow(new RuntimeException("Clinic not found"));

        mockMvc.perform(put("/api/clinics/clinic-999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinic)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/clinics/{id}")
    void deleteClinic_ShouldDelete() throws Exception {
        doNothing().when(clinicService).deleteClinic("clinic-001");

        mockMvc.perform(delete("/api/clinics/clinic-001"))
                .andExpect(status().isNoContent());
    }
}

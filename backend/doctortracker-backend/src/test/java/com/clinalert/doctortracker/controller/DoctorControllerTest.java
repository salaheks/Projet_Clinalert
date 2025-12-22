package com.clinalert.doctortracker.controller;

/**
 * Tests d'intégration pour DoctorController (utilise SpringBootTest pour éviter conflits sécurité)
 */

import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.service.DoctorService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests Controller Doctor - Integration")
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorService doctorService;

    private Doctor doctor1;
    private Doctor doctor2;

    @BeforeEach
    void setUp() {
        doctor1 = new Doctor();
        doctor1.setId("doctor-001");
        doctor1.setName("Dr. Jean Dupont");
        doctor1.setSpecialty("Cardiologie");
        doctor1.setEmail("jean.dupont@clinalert.com");
        doctor1.setPhoneNumber("+33 6 12 34 56 78");

        doctor2 = new Doctor();
        doctor2.setId("doctor-002");
        doctor2.setName("Dr. Marie Martin");
        doctor2.setSpecialty("Neurologie");
        doctor2.setEmail("marie.martin@clinalert.com");
        doctor2.setPhoneNumber("+33 6 98 76 54 32");
    }

    @Test
    @DisplayName("GET /api/doctors - Doit retourner tous les docteurs")
    void getAllDoctors_ShouldReturnAllDoctors() throws Exception {
        List<Doctor> doctors = Arrays.asList(doctor1, doctor2);
        when(doctorService.getAllDoctors()).thenReturn(doctors);

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Dr. Jean Dupont"))
                .andExpect(jsonPath("$[1].name").value("Dr. Marie Martin"));

        verify(doctorService, times(1)).getAllDoctors();
    }

    @Test
    @DisplayName("GET /api/doctors/{id} - Doit retourner le docteur")
    void getDoctorById_WhenExists_ShouldReturnDoctor() throws Exception {
        when(doctorService.getDoctorById("doctor-001")).thenReturn(Optional.of(doctor1));

        mockMvc.perform(get("/api/doctors/doctor-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doctor-001"))
                .andExpect(jsonPath("$.name").value("Dr. Jean Dupont"))
                .andExpect(jsonPath("$.specialty").value("Cardiologie"));

        verify(doctorService, times(1)).getDoctorById("doctor-001");
    }

    @Test
    @DisplayName("GET /api/doctors/{id} - Doit retourner 404 si non trouvé")
    void getDoctorById_WhenNotFound_ShouldReturn404() throws Exception {
        when(doctorService.getDoctorById("doctor-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/doctors/doctor-999"))
                .andExpect(status().isNotFound());

        verify(doctorService, times(1)).getDoctorById("doctor-999");
    }

    @Test
    @DisplayName("POST /api/doctors - Doit créer un nouveau docteur")
    void createDoctor_WithValidData_ShouldReturnCreated() throws Exception {
        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(doctor1);

        mockMvc.perform(post("/api/doctors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctor1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doctor-001"))
                .andExpect(jsonPath("$.name").value("Dr. Jean Dupont"));

        verify(doctorService, times(1)).createDoctor(any(Doctor.class));
    }

    @Test
    @DisplayName("PUT /api/doctors/{id} - Doit mettre à jour le docteur")
    void updateDoctor_WhenExists_ShouldReturnUpdated() throws Exception {
        Doctor updatedDoctor = new Doctor();
        updatedDoctor.setId("doctor-001");
        updatedDoctor.setName("Dr. Jean Dupont (Modifié)");
        updatedDoctor.setSpecialty("Cardiologie Interventionnelle");

        when(doctorService.updateDoctor(eq("doctor-001"), any(Doctor.class)))
                .thenReturn(updatedDoctor);

        mockMvc.perform(put("/api/doctors/doctor-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDoctor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Jean Dupont (Modifié)"))
                .andExpect(jsonPath("$.specialty").value("Cardiologie Interventionnelle"));

        verify(doctorService, times(1)).updateDoctor(eq("doctor-001"), any(Doctor.class));
    }

    @Test
    @DisplayName("PUT /api/doctors/{id} - Doit retourner 404 si non trouvé")
    void updateDoctor_WhenNotFound_ShouldReturn404() throws Exception {
        when(doctorService.updateDoctor(eq("doctor-999"), any(Doctor.class)))
                .thenThrow(new RuntimeException("Doctor not found"));

        mockMvc.perform(put("/api/doctors/doctor-999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctor1)))
                .andExpect(status().isNotFound());

        verify(doctorService, times(1)).updateDoctor(eq("doctor-999"), any(Doctor.class));
    }

    @Test
    @DisplayName("DELETE /api/doctors/{id} - Doit supprimer le docteur")
    void deleteDoctor_ShouldReturnOk() throws Exception {
        doNothing().when(doctorService).deleteDoctor("doctor-001");

        mockMvc.perform(delete("/api/doctors/doctor-001"))
                .andExpect(status().isOk());

        verify(doctorService, times(1)).deleteDoctor("doctor-001");
    }

    @Test
    @DisplayName("GET /api/doctors - Doit retourner liste vide")
    void getAllDoctors_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        when(doctorService.getAllDoctors()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(doctorService, times(1)).getAllDoctors();
    }
}

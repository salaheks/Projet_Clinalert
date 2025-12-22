package com.clinalert.doctortracker.controller;

/**
 * Tests d'intégration pour AlertController (utilise SpringBootTest pour éviter conflits sécurité)
 */

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests Controller Alert - Integration")
class AlertControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertService alertService;

    private Alert alert1;
    private Alert alert2;
    private Alert alert3;

    @BeforeEach
    void setUp() {
        alert1 = new Alert();
        alert1.setId("alert-001");
        alert1.setPatientId("patient-001");
        alert1.setMessage("Rythme cardiaque élevé");
        alert1.setSeverity("CRITICAL");
        alert1.setTimestamp(LocalDateTime.now());
        alert1.setRead(false);

        alert2 = new Alert();
        alert2.setId("alert-002");
        alert2.setPatientId("patient-001");
        alert2.setMessage("Température élevée");
        alert2.setSeverity("MEDIUM");
        alert2.setRead(true);

        alert3 = new Alert();
        alert3.setId("alert-003");
        alert3.setPatientId("patient-002");
        alert3.setMessage("Rappel médicament");
        alert3.setSeverity("LOW");
        alert3.setRead(false);
    }

    @Test
    @DisplayName("GET /api/alerts - Doit retourner toutes les alertes")
    void getAllAlerts_ShouldReturnAllAlerts() throws Exception {
        List<Alert> alerts = Arrays.asList(alert1, alert2, alert3);
        when(alertService.getAllAlerts()).thenReturn(alerts);

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].message").value("Rythme cardiaque élevé"));

        verify(alertService, times(1)).getAllAlerts();
    }

    @Test
    @DisplayName("GET /api/alerts/patient/{id} - Doit retourner alertes du patient")
    void getAlertsByPatient_ShouldReturnPatientAlerts() throws Exception {
        List<Alert> patientAlerts = Arrays.asList(alert1, alert2);
        when(alertService.getAlertsByPatientId("patient-001")).thenReturn(patientAlerts);

        mockMvc.perform(get("/api/alerts/patient/patient-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].patientId").value("patient-001"));

        verify(alertService, times(1)).getAlertsByPatientId("patient-001");
    }

    @Test
    @DisplayName("GET /api/alerts/unread - Doit retourner alertes non lues")
    void getUnreadAlerts_ShouldReturnUnreadAlerts() throws Exception {
        List<Alert> unreadAlerts = Arrays.asList(alert1, alert3);
        when(alertService.getUnreadAlerts()).thenReturn(unreadAlerts);

        mockMvc.perform(get("/api/alerts/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(alertService, times(1)).getUnreadAlerts();
    }

    @Test
    @DisplayName("PUT /api/alerts/{id}/read - Doit marquer comme lue")
    void markAsRead_ShouldReturnOk() throws Exception {
        doNothing().when(alertService).markAsRead("alert-001");

        mockMvc.perform(put("/api/alerts/alert-001/read"))
                .andExpect(status().isOk());

        verify(alertService, times(1)).markAsRead("alert-001");
    }

    @Test
    @DisplayName("GET /api/alerts - Doit retourner liste vide")
    void getAllAlerts_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        when(alertService.getAllAlerts()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(alertService, times(1)).getAllAlerts();
    }

    @Test
    @DisplayName("GET /api/alerts/patient/{id} - Doit retourner liste vide")
    void getAlertsByPatient_WhenNoAlerts_ShouldReturnEmpty() throws Exception {
        when(alertService.getAlertsByPatientId("patient-999")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/alerts/patient/patient-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(alertService, times(1)).getAlertsByPatientId("patient-999");
    }
}

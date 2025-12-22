package com.clinalert.doctortracker.service;

/**
 * ============================================
 * Tests Unitaires pour AlertService
 * ============================================
 * 
 * Tests du service de gestion des alertes médicales.
 * 
 * MÉTHODES TESTÉES :
 * - getAllAlerts() : Récupère toutes les alertes
 * - getAlertsByPatientId() : Filtrer par patient
 * - getUnreadAlerts() : Alertes non lues
 * - createAlert() : Créer une nouvelle alerte
 * - markAsRead() : Marquer comme lue
 * 
 * @author ClinAlert Team
 * @version 1.0
 */

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires du Service Alert")
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private Alert alert1;
    private Alert alert2;
    private Alert alert3;

    @BeforeEach
    void setUp() {
        // Alerte 1 : Critique, non lue
        alert1 = new Alert();
        alert1.setId("alert-001");
        alert1.setPatientId("patient-001");
        alert1.setMeasurementId("measure-001");
        alert1.setMessage("Rythme cardiaque élevé : 150 bpm");
        alert1.setSeverity("CRITICAL");
        alert1.setTimestamp(LocalDateTime.now());
        alert1.setRead(false);

        // Alerte 2 : Moyenne, lue
        alert2 = new Alert();
        alert2.setId("alert-002");
        alert2.setPatientId("patient-001");
        alert2.setMeasurementId("measure-002");
        alert2.setMessage("Température légèrement élevée : 38.2°C");
        alert2.setSeverity("MEDIUM");
        alert2.setTimestamp(LocalDateTime.now().minusHours(2));
        alert2.setRead(true);

        // Alerte 3 : Faible, non lue, autre patient
        alert3 = new Alert();
        alert3.setId("alert-003");
        alert3.setPatientId("patient-002");
        alert3.setMeasurementId("measure-003");
        alert3.setMessage("Rappel de prise de médicament");
        alert3.setSeverity("LOW");
        alert3.setTimestamp(LocalDateTime.now().minusMinutes(30));
        alert3.setRead(false);
    }

    @Test
    @DisplayName("getAllAlerts - Doit retourner toutes les alertes")
    void getAllAlerts_ShouldReturnAllAlerts() {
        // Arrange
        List<Alert> allAlerts = Arrays.asList(alert1, alert2, alert3);
        when(alertRepository.findAll()).thenReturn(allAlerts);

        // Act
        List<Alert> result = alertService.getAllAlerts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).contains(alert1, alert2, alert3);
        verify(alertRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllAlerts - Doit retourner liste vide si aucune alerte")
    void getAllAlerts_WhenNoAlerts_ShouldReturnEmptyList() {
        // Arrange
        when(alertRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Alert> result = alertService.getAllAlerts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(alertRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAlertsByPatientId - Doit retourner les alertes du patient")
    void getAlertsByPatientId_ShouldReturnPatientAlerts() {
        // Arrange
        String patientId = "patient-001";
        List<Alert> patientAlerts = Arrays.asList(alert1, alert2);
        when(alertRepository.findByPatientId(patientId)).thenReturn(patientAlerts);

        // Act
        List<Alert> result = alertService.getAlertsByPatientId(patientId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(alert1, alert2);
        assertThat(result).allMatch(a -> a.getPatientId().equals(patientId));
        verify(alertRepository, times(1)).findByPatientId(patientId);
    }

    @Test
    @DisplayName("getAlertsByPatientId - Doit retourner liste vide si patient sans alertes")
    void getAlertsByPatientId_WhenNoAlerts_ShouldReturnEmpty() {
        // Arrange
        String patientId = "patient-999";
        when(alertRepository.findByPatientId(patientId)).thenReturn(Arrays.asList());

        // Act
        List<Alert> result = alertService.getAlertsByPatientId(patientId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(alertRepository, times(1)).findByPatientId(patientId);
    }

    @Test
    @DisplayName("getUnreadAlerts - Doit retourner seulement les alertes non lues")
    void getUnreadAlerts_ShouldReturnOnlyUnreadAlerts() {
        // Arrange
        List<Alert> unreadAlerts = Arrays.asList(alert1, alert3);
        when(alertRepository.findByIsReadFalse()).thenReturn(unreadAlerts);

        // Act
        List<Alert> result = alertService.getUnreadAlerts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(alert1, alert3);
        assertThat(result).allMatch(a -> !a.isRead());
        verify(alertRepository, times(1)).findByIsReadFalse();
    }

    @Test
    @DisplayName("getUnreadAlerts - Doit retourner liste vide si toutes lues")
    void getUnreadAlerts_WhenAllRead_ShouldReturnEmpty() {
        // Arrange
        when(alertRepository.findByIsReadFalse()).thenReturn(Arrays.asList());

        // Act
        List<Alert> result = alertService.getUnreadAlerts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(alertRepository, times(1)).findByIsReadFalse();
    }

    @Test
    @DisplayName("createAlert - Doit créer et retourner une nouvelle alerte")
    void createAlert_WithValidData_ShouldSaveAndReturn() {
        // Arrange
        Alert newAlert = new Alert();
        newAlert.setPatientId("patient-003");
        newAlert.setMessage("Test alerte");
        newAlert.setSeverity("HIGH");

        Alert savedAlert = new Alert();
        savedAlert.setId("alert-004");
        savedAlert.setPatientId("patient-003");
        savedAlert.setMessage("Test alerte");
        savedAlert.setSeverity("HIGH");
        savedAlert.setRead(false);

        when(alertRepository.save(any(Alert.class))).thenReturn(savedAlert);

        // Act
        Alert result = alertService.createAlert(newAlert);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("alert-004");
        assertThat(result.getMessage()).isEqualTo("Test alerte");
        assertThat(result.getSeverity()).isEqualTo("HIGH");
        assertThat(result.isRead()).isFalse();
        verify(alertRepository, times(1)).save(any(Alert.class));
    }

    @Test
    @DisplayName("markAsRead - Doit marquer l'alerte comme lue")
    void markAsRead_WhenAlertExists_ShouldUpdateToRead() {
        // Arrange
        String alertId = "alert-001";
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert1));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        alertService.markAsRead(alertId);

        // Assert
        assertThat(alert1.isRead()).isTrue();
        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, times(1)).save(alert1);
    }

    @Test
    @DisplayName("markAsRead - Ne doit rien faire si alerte inexistante")
    void markAsRead_WhenAlertNotFound_ShouldDoNothing() {
        // Arrange
        String alertId = "alert-999";
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        // Act
        assertThatCode(() -> alertService.markAsRead(alertId))
                .doesNotThrowAnyException();

        // Assert
        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    @DisplayName("markAsRead - Ne doit pas changer une alerte déjà lue")
    void markAsRead_WhenAlreadyRead_ShouldRemainRead() {
        // Arrange
        String alertId = "alert-002";
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert2));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        alertService.markAsRead(alertId);

        // Assert
        assertThat(alert2.isRead()).isTrue();
        verify(alertRepository, times(1)).findById(alertId);
        verify(alertRepository, times(1)).save(alert2);
    }

    @Test
    @DisplayName("createAlert - Doit gérer les alertes de différentes sévérités")
    void createAlert_WithDifferentSeverities_ShouldSaveAll() {
        // Arrange & Act & Assert
        String[] severities = { "LOW", "MEDIUM", "HIGH", "CRITICAL" };

        for (String severity : severities) {
            Alert testAlert = new Alert();
            testAlert.setPatientId("patient-001");
            testAlert.setMessage("Test " + severity);
            testAlert.setSeverity(severity);

            when(alertRepository.save(any(Alert.class))).thenReturn(testAlert);

            Alert result = alertService.createAlert(testAlert);

            assertThat(result.getSeverity()).isEqualTo(severity);
        }

        verify(alertRepository, times(4)).save(any(Alert.class));
    }

    @Test
    @DisplayName("getAllAlerts - Doit retourner alertes triées par timestamp")
    void getAllAlerts_ShouldMaintainOrder() {
        // Arrange
        List<Alert> sortedAlerts = Arrays.asList(alert2, alert3, alert1);
        when(alertRepository.findAll()).thenReturn(sortedAlerts);

        // Act
        List<Alert> result = alertService.getAllAlerts();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly(alert2, alert3, alert1);
        verify(alertRepository, times(1)).findAll();
    }
}

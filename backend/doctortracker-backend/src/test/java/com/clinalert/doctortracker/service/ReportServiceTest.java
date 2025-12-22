package com.clinalert.doctortracker.service;

/**
 * Tests ReportService - 5 tests
 * Couvre: génération PDF reports
 */

import com.clinalert.doctortracker.model.*;
import com.clinalert.doctortracker.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Service Report")
class ReportServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private ReportService reportService;

    private Patient patient;
    private Measurement measurement;
    private Alert alert;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId("patient-001");
        patient.setName("Test Patient");
        patient.setAge(45);

        measurement = new Measurement();
        measurement.setType("Heart Rate");
        measurement.setValue(75.0);
        measurement.setTimestamp(LocalDateTime.now());

        alert = new Alert();
        alert.setMessage("Test Alert");
        alert.setSeverity("HIGH");
        alert.setTimestamp(LocalDateTime.now());
    }

    @Test
    @DisplayName("generatePatientReport - Avec données")
    void generateReport_WithData_ShouldGeneratePDF() {
        when(patientRepository.findById("patient-001")).thenReturn(Optional.of(patient));
        when(measurementRepository.findTop20ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList(measurement));
        when(alertRepository.findTop10ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList(alert));

        byte[] result = reportService.generatePatientReport("patient-001");

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("generatePatientReport - Patient non trouvé")
    void generateReport_PatientNotFound_ShouldThrow() {
        when(patientRepository.findById("patient-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.generatePatientReport("patient-999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Patient not found");
    }

    @Test
    @DisplayName("generatePatientReport - Sans mesures")
    void generateReport_NoMeasurements_ShouldGeneratePDF() {
        when(patientRepository.findById("patient-001")).thenReturn(Optional.of(patient));
        when(measurementRepository.findTop20ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList());
        when(alertRepository.findTop10ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList(alert));

        byte[] result = reportService.generatePatientReport("patient-001");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("generatePatientReport - Sans alertes")
    void generateReport_NoAlerts_ShouldGeneratePDF() {
        when(patientRepository.findById("patient-001")).thenReturn(Optional.of(patient));
        when(measurementRepository.findTop20ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList(measurement));
        when(alertRepository.findTop10ByPatientIdOrderByTimestampDesc("patient-001"))
                .thenReturn(Arrays.asList());

        byte[] result = reportService.generatePatientReport("patient-001");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("generatePatientReport - ID null")
    void generateReport_NullId_ShouldThrow() {
        assertThatThrownBy(() -> reportService.generatePatientReport(null))
                .isInstanceOf(NullPointerException.class);
    }
}

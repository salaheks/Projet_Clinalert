package com.clinalert.doctortracker.service;

/**
 * Tests MeasurementService - 8 tests
 * Couvre: save measurements, getHistory, alert detection
 */

import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.repository.MeasurementRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Service Measurement")
class MeasurementServiceTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private AlertService alertService;

    @Mock
    private AnomalyDetectionService anomalyDetectionService;

    @InjectMocks
    private MeasurementService measurementService;

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
    @DisplayName("saveMeasurements - Normal values")
    void saveMeasurements_NormalValues_ShouldSave() {
        when(measurementRepository.saveAll(anyList())).thenReturn(Arrays.asList(measurement));
        when(anomalyDetectionService.isAnomaly(any())).thenReturn(false);

        List<Measurement> result = measurementService.saveMeasurements(Arrays.asList(measurement));

        assertThat(result).hasSize(1);
        verify(measurementRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("saveMeasurements - High heart rate")
    void saveMeasurements_HighHeartRate_ShouldCreateAlert() {
        measurement.setValue(120.0);
        when(measurementRepository.saveAll(anyList())).thenReturn(Arrays.asList(measurement));
        when(anomalyDetectionService.isAnomaly(any())).thenReturn(false);

        measurementService.saveMeasurements(Arrays.asList(measurement));

        verify(alertService, atLeastOnce()).createAlert(any());
    }

    @Test
    @DisplayName("saveMeasurements - Low heart rate")
    void saveMeasurements_LowHeartRate_ShouldCreateAlert() {
        measurement.setValue(40.0);
        when(measurementRepository.saveAll(anyList())).thenReturn(Arrays.asList(measurement));
        when(anomalyDetectionService.isAnomaly(any())).thenReturn(false);

        measurementService.saveMeasurements(Arrays.asList(measurement));

        verify(alertService, atLeastOnce()).createAlert(any());
    }

    @Test
    @DisplayName("saveMeasurements - Low SpO2")
    void saveMeasurements_LowSpO2_ShouldCreateCriticalAlert() {
        measurement.setType("SpO2");
        measurement.setValue(85.0);
        when(measurementRepository.saveAll(anyList())).thenReturn(Arrays.asList(measurement));
        when(anomalyDetectionService.isAnomaly(any())).thenReturn(false);

        measurementService.saveMeasurements(Arrays.asList(measurement));

        verify(alertService, atLeastOnce()).createAlert(any());
    }

    @Test
    @DisplayName("saveMeasurements - Anomaly detected")
    void saveMeasurements_AnomalyDetected_ShouldCreateWarning() {
        when(measurementRepository.saveAll(anyList())).thenReturn(Arrays.asList(measurement));
        when(anomalyDetectionService.isAnomaly(any())).thenReturn(true);

        measurementService.saveMeasurements(Arrays.asList(measurement));

        verify(alertService, atLeastOnce()).createAlert(any());
    }

    @Test
    @DisplayName("getHistory")
    void getHistory_ShouldReturnMeasurements() {
        when(measurementRepository.findByPatientId("patient-001"))
                .thenReturn(Arrays.asList(measurement));

        List<Measurement> result = measurementService.getHistory("patient-001");

        assertThat(result).hasSize(1);
        verify(measurementRepository).findByPatientId("patient-001");
    }

    @Test
    @DisplayName("getHistory - Empty")
    void getHistory_WhenEmpty_ShouldReturnEmpty() {
        when(measurementRepository.findByPatientId("patient-999"))
                .thenReturn(Arrays.asList());

        List<Measurement> result = measurementService.getHistory("patient-999");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("saveMeasurements - Multiple measurements")
    void saveMeasurements_Multiple_ShouldSaveAll() {
        Measurement m2 = new Measurement();
        m2.setValue(80.0);
        when(measurementRepository.saveAll(anyList())).thenReturn(Arrays.asList(measurement, m2));
        when(anomalyDetectionService.isAnomaly(any())).thenReturn(false);

        List<Measurement> result = measurementService.saveMeasurements(Arrays.asList(measurement, m2));

        assertThat(result).hasSize(2);
        verify(measurementRepository).saveAll(anyList());
    }
}

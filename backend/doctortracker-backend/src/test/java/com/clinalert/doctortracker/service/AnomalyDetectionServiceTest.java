package com.clinalert.doctortracker.service;

/**
 * Tests AnomalyDetectionService - 6 tests
 * Couvre: Statistical anomaly detection logic
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests AnomalyDetectionService")
class AnomalyDetectionServiceTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private AnomalyDetectionService anomalyDetectionService;

    private Measurement newMeasurement;
    private List<Measurement> normalHistory;

    @BeforeEach
    void setUp() {
        newMeasurement = new Measurement();
        newMeasurement.setPatientId("patient-001");
        newMeasurement.setType("Heart Rate");
        newMeasurement.setValue(75.0);
        newMeasurement.setTimestamp(LocalDateTime.now());

        // Create normal history (60-80 bpm range)
        normalHistory = createMeasurementHistory("patient-001", "Heart Rate",
                Arrays.asList(70.0, 72.0, 68.0, 75.0, 73.0, 71.0, 69.0));
    }

    @Test
    @DisplayName("Normal value should NOT be anomaly")
    void isAnomaly_NormalValue_ShouldReturnFalse() {
        when(measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                "patient-001", "Heart Rate")).thenReturn(normalHistory);

        newMeasurement.setValue(72.0); // Within normal range

        boolean result = anomalyDetectionService.isAnomaly(newMeasurement);

        assertFalse(result, "Normal value should not be detected as anomaly");
    }

    @Test
    @DisplayName("Extremely high value should BE anomaly")
    void isAnomaly_ExtremelyHighValue_ShouldReturnTrue() {
        when(measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                "patient-001", "Heart Rate")).thenReturn(normalHistory);

        newMeasurement.setValue(150.0); // Way above normal (mean ~71, this is +79)

        boolean result = anomalyDetectionService.isAnomaly(newMeasurement);

        assertTrue(result, "Extremely high value should be detected as anomaly");
    }

    @Test
    @DisplayName("Extremely low value should BE anomaly")
    void isAnomaly_ExtremelyLowValue_ShouldReturnTrue() {
        when(measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                "patient-001", "Heart Rate")).thenReturn(normalHistory);

        newMeasurement.setValue(30.0); // Way below normal

        boolean result = anomalyDetectionService.isAnomaly(newMeasurement);

        assertTrue(result, "Extremely low value should be detected as anomaly");
    }

    @Test
    @DisplayName("Insufficient history should NOT trigger anomaly")
    void isAnomaly_InsufficientHistory_ShouldReturnFalse() {
        List<Measurement> shortHistory = createMeasurementHistory("patient-001", "Heart Rate",
                Arrays.asList(70.0, 72.0)); // Only 2 values

        when(measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                "patient-001", "Heart Rate")).thenReturn(shortHistory);

        newMeasurement.setValue(150.0); // Even extreme value

        boolean result = anomalyDetectionService.isAnomaly(newMeasurement);

        assertFalse(result, "Insufficient history should not trigger anomaly detection");
    }

    @Test
    @DisplayName("Stable values with sudden spike should BE anomaly")
    void isAnomaly_StableValuesWithSpike_ShouldReturnTrue() {
        // Very stable history (std dev < 0.1)
        List<Measurement> stableHistory = createMeasurementHistory("patient-001", "Heart Rate",
                Arrays.asList(70.0, 70.0, 70.0, 70.0, 70.0, 70.0));

        when(measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                "patient-001", "Heart Rate")).thenReturn(stableHistory);

        newMeasurement.setValue(90.0); // +28.6% deviation (> 20% threshold)

        boolean result = anomalyDetectionService.isAnomaly(newMeasurement);

        assertTrue(result, "Spike in stable data should be detected as anomaly");
    }

    @Test
    @DisplayName("Small deviation in stable data should NOT be anomaly")
    void isAnomaly_SmallDeviationInStableData_ShouldReturnFalse() {
        List<Measurement> stableHistory = createMeasurementHistory("patient-001", "Heart Rate",
                Arrays.asList(70.0, 70.0, 70.0, 70.0, 70.0, 70.0));

        when(measurementRepository.findTop20ByPatientIdAndTypeOrderByTimestampDesc(
                "patient-001", "Heart Rate")).thenReturn(stableHistory);

        newMeasurement.setValue(75.0); // Only +7% deviation (< 20% threshold)

        boolean result = anomalyDetectionService.isAnomaly(newMeasurement);

        assertFalse(result, "Small deviation in stable data should not trigger anomaly");
    }

    // Helper method to create measurement history
    private List<Measurement> createMeasurementHistory(String patientId, String type, List<Double> values) {
        List<Measurement> history = new ArrayList<>();
        for (Double value : values) {
            Measurement m = new Measurement();
            m.setPatientId(patientId);
            m.setType(type);
            m.setValue(value);
            m.setTimestamp(LocalDateTime.now().minusMinutes(history.size()));
            history.add(m);
        }
        return history;
    }
}

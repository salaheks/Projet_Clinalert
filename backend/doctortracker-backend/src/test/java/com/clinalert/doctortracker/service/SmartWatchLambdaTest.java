package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.DailyHealthSummary;
import com.clinalert.doctortracker.model.HealthData;
import com.clinalert.doctortracker.repository.HealthDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Lambda Coverage Tests for SmartWatchHealthService
 * Uses REAL HealthData objects (not mocks) to trigger lambda execution in
 * stream operations
 * Target: Execute ALL lambdas in generateDailySummary
 * Impact: +3-4% coverage (386 missed instructions)
 */
@SpringBootTest
@DisplayName("SmartWatch Lambda Coverage Tests")
class SmartWatchHealthServiceLambdaTest {

    @Autowired
    private SmartWatchHealthService service;

    @MockBean
    private HealthDataRepository healthDataRepository;

    private String patientId;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        patientId = "patient-lambda-001";
        date = LocalDate.now();
    }

    /**
     * Create REAL HealthData object (not mock) to ensure lambdas execute
     */
    private HealthData createHealthData(Integer hr, Integer steps, Double spO2, Integer sleep,
            Integer sys, Integer dia, Integer cal, Double dist, Double temp) {
        HealthData data = new HealthData();
        data.setPatientId(patientId);
        data.setTimestamp(LocalDateTime.now());
        data.setHeartRate(hr);
        data.setSteps(steps);
        data.setSpO2(spO2);
        data.setSleepMinutes(sleep);
        data.setBloodPressureSystolic(sys);
        data.setBloodPressureDiastolic(dia);
        data.setCaloriesBurned(cal);
        data.setDistanceMeters(dist);
        data.setTemperature(temp);
        return data;
    }

    @Test
    @DisplayName("Heart rate lambdas - filter, map, average, min, max")
    void heartRateLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(65, null, null, null, null, null, null, null, null),
                createHealthData(75, null, null, null, null, null, null, null, null),
                createHealthData(85, null, null, null, null, null, null, null, null),
                createHealthData(95, null, null, null, null, null, null, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        // Verify lambda calculations
        assertNotNull(result);
        assertEquals(80.0, result.getAvgHeartRate(), 0.1); // (65+75+85+95)/4
        assertEquals(65, result.getMinHeartRate());
        assertEquals(95, result.getMaxHeartRate());
        assertEquals(4, result.getDataPointsCount());
    }

    @Test
    @DisplayName("Steps lambdas - filter, mapToInt, sum")
    void stepsLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, 2000, null, null, null, null, null, null, null),
                createHealthData(null, 3500, null, null, null, null, null, null, null),
                createHealthData(null, 4500, null, null, null, null, null, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(10000, result.getTotalSteps()); // 2000+3500+4500
    }

    @Test
    @DisplayName("SpO2 lambdas - filter, map, mapToDouble, average, min")
    void spO2Lambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, null, 95.0, null, null, null, null, null, null),
                createHealthData(null, null, 97.0, null, null, null, null, null, null),
                createHealthData(null, null, 99.0, null, null, null, null, null, null),
                createHealthData(null, null, 96.0, null, null, null, null, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(96.75, result.getAvgSpO2(), 0.1); // (95+97+99+96)/4
        assertEquals(95.0, result.getMinSpO2(), 0.1);
    }

    @Test
    @DisplayName("Sleep lambdas - filter, mapToInt, sum")
    void sleepLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, null, null, 420, null, null, null, null, null),
                createHealthData(null, null, null, 480, null, null, null, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(900, result.getTotalSleepMinutes()); // 420+480
    }

    @Test
    @DisplayName("Blood pressure lambdas - filter, map, mapToInt, average")
    void bloodPressureLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, null, null, null, 115, 75, null, null, null),
                createHealthData(null, null, null, null, 120, 80, null, null, null),
                createHealthData(null, null, null, null, 125, 85, null, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(120.0, result.getAvgSystolic(), 0.1); // (115+120+125)/3
        assertEquals(80.0, result.getAvgDiastolic(), 0.1); // (75+80+85)/3
    }

    @Test
    @DisplayName("Calories lambdas - filter, mapToInt, sum")
    void caloriesLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, null, null, null, null, null, 150, null, null),
                createHealthData(null, null, null, null, null, null, 200, null, null),
                createHealthData(null, null, null, null, null, null, 250, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(600, result.getTotalCaloriesBurned()); // 150+200+250
    }

    @Test
    @DisplayName("Distance lambdas - filter, mapToDouble, sum")
    void distanceLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, null, null, null, null, null, null, 1000.0, null),
                createHealthData(null, null, null, null, null, null, null, 1500.0, null),
                createHealthData(null, null, null, null, null, null, null, 2500.0, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(5000.0, result.getTotalDistanceMeters(), 0.1); // 1000+1500+2500
    }

    @Test
    @DisplayName("Temperature lambdas - filter, map, mapToDouble, average")
    void temperatureLambdas_ShouldExecute() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(null, null, null, null, null, null, null, null, 36.5),
                createHealthData(null, null, null, null, null, null, null, null, 36.7),
                createHealthData(null, null, null, null, null, null, null, null, 36.6));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertEquals(36.6, result.getAvgTemperature(), 0.1); // (36.5+36.7+36.6)/3
    }

    @Test
    @DisplayName("ALL lambdas together - comprehensive mixed data test")
    void allLambdasTogether_WithMixedData_ShouldExecuteAll() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(70, 5000, 96.0, 420, 118, 78, 180, 2000.0, 36.5),
                createHealthData(80, 6000, 98.0, 480, 122, 82, 220, 2500.0, 36.7),
                createHealthData(75, 4500, 97.0, null, 120, 80, 200, 1500.0, 36.6));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        // ALL lambda operations executed
        assertNotNull(result);
        assertEquals(75.0, result.getAvgHeartRate(), 0.1);
        assertEquals(70, result.getMinHeartRate());
        assertEquals(80, result.getMaxHeartRate());
        assertEquals(15500, result.getTotalSteps());
        assertEquals(97.0, result.getAvgSpO2(), 0.1);
        assertEquals(96.0, result.getMinSpO2(), 0.1);
        assertEquals(900, result.getTotalSleepMinutes());
        assertEquals(120.0, result.getAvgSystolic(), 0.1);
        assertEquals(80.0, result.getAvgDiastolic(), 0.1);
        assertEquals(600, result.getTotalCaloriesBurned());
        assertEquals(6000.0, result.getTotalDistanceMeters(), 0.1);
        assertEquals(36.6, result.getAvgTemperature(), 0.1);
        assertEquals(3, result.getDataPointsCount());
    }

    @Test
    @DisplayName("Empty data - Should return null without executing lambdas")
    void emptyData_ShouldReturnNull() {
        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(Collections.emptyList());

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        assertNull(result);
    }

    @Test
    @DisplayName("Partial null values - Lambdas should filter correctly")
    void partialNullValues_LambdasShouldFilter() {
        List<HealthData> realData = Arrays.asList(
                createHealthData(70, null, 96.0, null, null, null, null, null, null),
                createHealthData(null, 5000, null, 420, null, null, null, null, null),
                createHealthData(80, null, null, null, 120, 80, null, null, null));

        when(healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(
                anyString(), any(), any())).thenReturn(realData);

        DailyHealthSummary result = service.generateDailySummary(patientId, date);

        // Only non-null values processed by lambdas
        assertEquals(75.0, result.getAvgHeartRate(), 0.1); // (70+80)/2
        assertEquals(5000, result.getTotalSteps()); // only one value
        assertEquals(96.0, result.getAvgSpO2(), 0.1); // only one value
        assertEquals(420, result.getTotalSleepMinutes()); // only one value
        assertEquals(120.0, result.getAvgSystolic(), 0.1); // only one value
    }
}

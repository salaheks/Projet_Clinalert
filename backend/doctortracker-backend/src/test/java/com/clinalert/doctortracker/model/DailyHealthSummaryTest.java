package com.clinalert.doctortracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DailyHealthSummary Model
 * Target: Cover all getters and setters (91 missed instructions)
 * Impact: +1.5% coverage
 */
@DisplayName("Model Tests - DailyHealthSummary")
class DailyHealthSummaryTest {

    private DailyHealthSummary summary;

    @BeforeEach
    void setUp() {
        summary = new DailyHealthSummary();
    }

    @Test
    @DisplayName("All setters and getters should work correctly")
    void allGettersAndSetters_ShouldWork() {
        LocalDate date = LocalDate.now();

        summary.setId("summary-123");
        summary.setPatientId("patient-001");
        summary.setDate(date);
        summary.setDataPointsCount(50);
        summary.setAvgHeartRate(75.5);
        summary.setMinHeartRate(60);
        summary.setMaxHeartRate(90);
        summary.setTotalSteps(10000);
        summary.setTotalSleepMinutes(480);
        summary.setAvgSpO2(98.5);
        summary.setMinSpO2(95.0);
        summary.setAvgSystolic(120.0);
        summary.setAvgDiastolic(80.0);

        // Verify ALL getters
        assertEquals("summary-123", summary.getId());
        assertEquals("patient-001", summary.getPatientId());
        assertEquals(date, summary.getDate());
        assertEquals(50, summary.getDataPointsCount());
        assertEquals(75.5, summary.getAvgHeartRate());
        assertEquals(60, summary.getMinHeartRate());
        assertEquals(90, summary.getMaxHeartRate());
        assertEquals(10000, summary.getTotalSteps());
        assertEquals(480, summary.getTotalSleepMinutes());
        assertEquals(98.5, summary.getAvgSpO2());
        assertEquals(95.0, summary.getMinSpO2());
        assertEquals(120.0, summary.getAvgSystolic());
        assertEquals(80.0, summary.getAvgDiastolic());
    }

    @Test
    @DisplayName("Default constructor should create empty object")
    void defaultConstructor_ShouldWork() {
        DailyHealthSummary newSummary = new DailyHealthSummary();
        assertNotNull(newSummary);
    }

    @Test
    @DisplayName("Can set null values")
    void nullValues_ShouldBeAllowed() {
        summary.setId(null);
        summary.setPatientId(null);
        summary.setDate(null);

        assertNull(summary.getId());
        assertNull(summary.getPatientId());
        assertNull(summary.getDate());
    }

    @Test
    @DisplayName("Edge case values should work")
    void edgeCaseValues_ShouldWork() {
        summary.setAvgHeartRate(0.0);
        summary.setMinHeartRate(0);
        summary.setMaxHeartRate(Integer.MAX_VALUE);
        summary.setTotalSteps(0);

        assertEquals(0.0, summary.getAvgHeartRate());
        assertEquals(0, summary.getMinHeartRate());
        assertEquals(Integer.MAX_VALUE, summary.getMaxHeartRate());
        assertEquals(0, summary.getTotalSteps());
    }
}

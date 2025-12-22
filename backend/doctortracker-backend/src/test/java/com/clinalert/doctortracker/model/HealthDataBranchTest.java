package com.clinalert.doctortracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HealthData model - covering branches
 */
@DisplayName("HealthData Model Branch Tests")
class HealthDataBranchTest {

    @Test
    @DisplayName("All getters and setters should work")
    void allGettersSetters_ShouldWork() {
        HealthData data = new HealthData();

        data.setId("hd-123");
        data.setPatientId("patient-456");
        data.setHeartRate(75);
        data.setSteps(10000);
        data.setSpO2(98.0);
        data.setSleepMinutes(480);
        data.setBloodPressureSystolic(120);
        data.setBloodPressureDiastolic(80);
        data.setCaloriesBurned(300);
        data.setDistanceMeters(5000.0);
        data.setTemperature(36.6);

        assertEquals("hd-123", data.getId());
        assertEquals("patient-456", data.getPatientId());
        assertEquals(75, data.getHeartRate());
        assertEquals(10000, data.getSteps());
        assertEquals(98.0, data.getSpO2());
        assertEquals(480, data.getSleepMinutes());
        assertEquals(120, data.getBloodPressureSystolic());
        assertEquals(80, data.getBloodPressureDiastolic());
        assertEquals(300, data.getCaloriesBurned());
        assertEquals(5000.0, data.getDistanceMeters());
        assertEquals(36.6, data.getTemperature());
    }

    @Test
    @DisplayName("Null values should be allowed")
    void nullValues_ShouldBeAllowed() {
        HealthData data = new HealthData();

        data.setHeartRate(null);
        data.setSteps(null);
        data.setSpO2(null);

        assertNull(data.getHeartRate());
        assertNull(data.getSteps());
        assertNull(data.getSpO2());
    }

    @Test
    @DisplayName("Edge case values should work")
    void edgeCaseValues_ShouldWork() {
        HealthData data = new HealthData();

        data.setHeartRate(0);
        data.setSteps(Integer.MAX_VALUE);
        data.setSpO2(0.0);

        assertEquals(0, data.getHeartRate());
        assertEquals(Integer.MAX_VALUE, data.getSteps());
        assertEquals(0.0, data.getSpO2());
    }
}

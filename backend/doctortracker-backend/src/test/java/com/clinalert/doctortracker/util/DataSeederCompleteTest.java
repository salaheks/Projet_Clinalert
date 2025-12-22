package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.repository.AlertRepository;
import com.clinalert.doctortracker.repository.DoctorRepository;
import com.clinalert.doctortracker.repository.MeasurementRepository;
import com.clinalert.doctortracker.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive DataSeeder tests with CORRECT method signatures
 * Target: 13% → 70%+ (+10% total coverage)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DataSeeder Comprehensive Tests")
class DataSeederCompleteTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AlertRepository alertRepository;

    private DataSeeder seeder;

    @BeforeEach
    void setUp() {
        seeder = new DataSeeder(
                measurementRepository,
                doctorRepository,
                patientRepository,
                alertRepository);
    }

    @Test
    @DisplayName("createMeasurement - Heart rate - Should create with correct values")
    void createMeasurement_HeartRate_ShouldCreate() throws Exception {
        Method method = DataSeeder.class.getDeclaredMethod(
                "createMeasurement", String.class, String.class, Double.class, int.class);
        method.setAccessible(true);

        Measurement result = (Measurement) method.invoke(
                seeder, "patient-123", "heart_rate", 72.0, 30);

        assertNotNull(result);
        assertEquals("patient-123", result.getPatientId());
        assertEquals("heart_rate", result.getType());
        assertEquals(72.0, result.getValue());
        assertEquals("device-001", result.getDeviceId());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("createMeasurement - Temperature - Should create")
    void createMeasurement_Temperature_ShouldCreate() throws Exception {
        Method method = DataSeeder.class.getDeclaredMethod(
                "createMeasurement", String.class, String.class, Double.class, int.class);
        method.setAccessible(true);

        Measurement result = (Measurement) method.invoke(
                seeder, "p1", "temperature", 36.6, 15);

        assertEquals("temperature", result.getType());
        assertEquals(36.6, result.getValue());
    }

    @Test
    @DisplayName("createMeasurement - Blood pressure - Should create")
    void createMeasurement_BloodPressure_ShouldCreate() throws Exception {
        Method method = DataSeeder.class.getDeclaredMethod(
                "createMeasurement", String.class, String.class, Double.class, int.class);
        method.setAccessible(true);

        Measurement result = (Measurement) method.invoke(
                seeder, "p1", "blood_pressure_systolic", 120.0, 8);

        assertEquals("blood_pressure_systolic", result.getType());
        assertEquals(120.0, result.getValue());
    }

    @Test
    @DisplayName("createMeasurement - Oxygen saturation - Should create")
    void createMeasurement_OxygenSaturation_ShouldCreate() throws Exception {
        Method method = DataSeeder.class.getDeclaredMethod(
                "createMeasurement", String.class, String.class, Double.class, int.class);
        method.setAccessible(true);

        Measurement result = (Measurement) method.invoke(
                seeder, "p1", "oxygen_saturation", 98.0, 5);

        assertEquals("oxygen_saturation", result.getType());
        assertEquals(98.0, result.getValue());
    }

    @Test
    @DisplayName("createAlert - HIGH severity - Should create")
    void createAlert_HighSeverity_ShouldCreate() throws Exception {
        Method method = DataSeeder.class.getDeclaredMethod(
                "createAlert", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Alert result = (Alert) method.invoke(
                seeder, "patient-123", "measurement-456", "High BP detected", "HIGH");

        assertNotNull(result);
        assertEquals("patient-123", result.getPatientId());
        assertEquals("measurement-456", result.getMeasurementId());
        assertEquals("High BP detected", result.getMessage());
        assertEquals("HIGH", result.getSeverity());
    }

    @Test
    @DisplayName("createAlert - All severities - LOW, MEDIUM, HIGH, CRITICAL")
    void createAlert_AllSeverities_ShouldWork() throws Exception {
        Method method = DataSeeder.class.getDeclaredMethod(
                "createAlert", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        Alert low = (Alert) method.invoke(seeder, "p1", "m1", "msg", "LOW");
        Alert med = (Alert) method.invoke(seeder, "p1", "m1", "msg", "MEDIUM");
        Alert high = (Alert) method.invoke(seeder, "p1", "m1", "msg", "HIGH");
        Alert crit = (Alert) method.invoke(seeder, "p1", "m1", "msg", "CRITICAL");

        assertEquals("LOW", low.getSeverity());
        assertEquals("MEDIUM", med.getSeverity());
        assertEquals("HIGH", high.getSeverity());
        assertEquals("CRITICAL", crit.getSeverity());
    }

    @Test
    @DisplayName("run - No data exists - Should create all seed data")
    void run_NoDataExists_ShouldCreateAll() throws Exception {
        // Setup: All repositories are empty
        when(doctorRepository.count()).thenReturn(0L);
        when(patientRepository.count()).thenReturn(0L);
        when(measurementRepository.count()).thenReturn(0L);
        when(alertRepository.count()).thenReturn(0L);

        // Mock saveAll to return saved entities (NEED 2 doctors!)
        Doctor doc1 = new Doctor();
        doc1.setId("doc-1");
        doc1.setName("Dr. Test 1");

        Doctor doc2 = new Doctor();
        doc2.setId("doc-2");
        doc2.setName("Dr. Test 2");
        when(doctorRepository.saveAll(any())).thenReturn(Arrays.asList(doc1, doc2));

        Patient pat = new Patient();
        pat.setId("pat-1");
        pat.setName("Test Patient");
        pat.setAge(45);
        when(patientRepository.saveAll(any())).thenReturn(Arrays.asList(pat));

        Measurement meas = new Measurement();
        meas.setId("meas-1");
        meas.setPatientId("pat-1");
        meas.setType("heart_rate");
        meas.setValue(75.0);
        when(measurementRepository.saveAll(any())).thenReturn(Arrays.asList(meas));

        // Execute
        seeder.run();

        // Verify all saves were called
        verify(doctorRepository).saveAll(any());
        verify(patientRepository).saveAll(any());
        verify(measurementRepository).saveAll(any());
        verify(alertRepository).saveAll(any());
    }

    @Test
    @DisplayName("run - All data exists - Should skip seeding")
    void run_AllDataExists_ShouldSkipSeeding() throws Exception {
        when(doctorRepository.count()).thenReturn(10L);
        when(patientRepository.count()).thenReturn(20L);
        when(measurementRepository.count()).thenReturn(100L);
        when(alertRepository.count()).thenReturn(50L);

        when(doctorRepository.findAll()).thenReturn(Collections.emptyList());
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());
        when(measurementRepository.findAll()).thenReturn(Collections.emptyList());

        seeder.run();

        // Should NOT save anything
        verify(doctorRepository, never()).saveAll(any());
        verify(patientRepository, never()).saveAll(any());
        verify(measurementRepository, never()).saveAll(any());
        verify(alertRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("run - Doctors exist but patients don't - Should skip doctors, create patients")
    void run_DoctorsExist_PatientsDoNot_ShouldCreatePatients() throws Exception {
        when(doctorRepository.count()).thenReturn(2L);
        when(patientRepository.count()).thenReturn(0L);
        when(measurementRepository.count()).thenReturn(0L);
        when(alertRepository.count()).thenReturn(0L);

        Doctor doc1 = new Doctor();
        doc1.setId("doc-1");
        Doctor doc2 = new Doctor();
        doc2.setId("doc-2");
        when(doctorRepository.findAll()).thenReturn(Arrays.asList(doc1, doc2));

        Patient pat = new Patient();
        pat.setId("pat-1");
        pat.setName("Test");
        pat.setAge(50);
        when(patientRepository.saveAll(any())).thenReturn(Arrays.asList(pat));

        Measurement m = new Measurement();
        m.setId("m-1");
        m.setPatientId("pat-1");
        m.setType("heart_rate");
        m.setValue(70.0);
        when(measurementRepository.saveAll(any())).thenReturn(Arrays.asList(m));

        seeder.run();

        verify(doctorRepository, never()).saveAll(any());
        verify(patientRepository).saveAll(any());
    }

    @Test
    @DisplayName("Constructor - Should initialize all repositories")
    void constructor_ShouldInitialize() {
        DataSeeder newSeeder = new DataSeeder(
                measurementRepository,
                doctorRepository,
                patientRepository,
                alertRepository);

        assertNotNull(newSeeder);
    }
}

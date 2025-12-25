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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
                seeder = new DataSeeder(measurementRepository, doctorRepository, patientRepository, alertRepository);
        }

        // --- Core Logic Tests (Reflection) ---

        @Test
        @DisplayName("Logic - Heart Rate Alert Evaluation")
        void checkHeartRateAlert_Logic() throws Exception {
                Method method = DataSeeder.class.getDeclaredMethod("checkHeartRateAlert", Measurement.class);
                method.setAccessible(true);

                // Case 1: High Severity (> 100)
                Measurement mHigh = createMockMeasurement("hr", 101.0);
                Alert aHigh = (Alert) method.invoke(seeder, mHigh);
                assertNotNull(aHigh);
                assertEquals(AppConstants.ALERT_SEVERITY_HIGH, aHigh.getSeverity());

                // Case 2: Medium Severity (> 90 but <= 100)
                Measurement mMed = createMockMeasurement("hr", 95.0);
                Alert aMed = (Alert) method.invoke(seeder, mMed);
                assertNotNull(aMed);
                assertEquals(AppConstants.ALERT_SEVERITY_MEDIUM, aMed.getSeverity());

                // Case 3: No Alert (<= 90)
                Measurement mNone = createMockMeasurement("hr", 90.0);
                assertNull(method.invoke(seeder, mNone));
        }

        @Test
        @DisplayName("Logic - Temperature Alert Evaluation")
        void checkTemperatureAlert_Logic() throws Exception {
                Method method = DataSeeder.class.getDeclaredMethod("checkTemperatureAlert", Measurement.class);
                method.setAccessible(true);

                // Case 1: High (> 38.0)
                Measurement mHigh = createMockMeasurement("temp", 38.1);
                Alert aHigh = (Alert) method.invoke(seeder, mHigh);
                assertEquals(AppConstants.ALERT_SEVERITY_HIGH, aHigh.getSeverity());

                // Case 2: Medium (> 37.5 <= 38.0)
                Measurement mMed = createMockMeasurement("temp", 37.8);
                Alert aMed = (Alert) method.invoke(seeder, mMed);
                assertEquals(AppConstants.ALERT_SEVERITY_MEDIUM, aMed.getSeverity());

                // Case 3: None (<= 37.5)
                Measurement mNone = createMockMeasurement("temp", 37.5);
                assertNull(method.invoke(seeder, mNone));
        }

        @Test
        @DisplayName("Logic - Blood Pressure Alert Evaluation")
        void checkBloodPressureAlert_Logic() throws Exception {
                Method method = DataSeeder.class.getDeclaredMethod("checkBloodPressureAlert", Measurement.class);
                method.setAccessible(true);

                // Case 1: Critical (> 160)
                Measurement mCrit = createMockMeasurement("bp", 161.0);
                Alert aCrit = (Alert) method.invoke(seeder, mCrit);
                assertEquals(AppConstants.ALERT_SEVERITY_CRITICAL, aCrit.getSeverity());

                // Case 2: High (> 140 <= 160)
                Measurement mHigh = createMockMeasurement("bp", 150.0);
                Alert aHigh = (Alert) method.invoke(seeder, mHigh);
                assertEquals(AppConstants.ALERT_SEVERITY_HIGH, aHigh.getSeverity());

                // Case 3: None (<= 140)
                Measurement mNone = createMockMeasurement("bp", 140.0);
                assertNull(method.invoke(seeder, mNone));
        }

        @Test
        @DisplayName("Logic - Oxygen Alert Evaluation")
        void checkOxygenAlert_Logic() throws Exception {
                Method method = DataSeeder.class.getDeclaredMethod("checkOxygenAlert", Measurement.class);
                method.setAccessible(true);

                // Case 1: Critical (< 90)
                Measurement mCrit = createMockMeasurement("o2", 89.0);
                Alert aCrit = (Alert) method.invoke(seeder, mCrit);
                assertEquals(AppConstants.ALERT_SEVERITY_CRITICAL, aCrit.getSeverity());

                // Case 2: Medium (< 95 >= 90)
                Measurement mMed = createMockMeasurement("o2", 92.0);
                Alert aMed = (Alert) method.invoke(seeder, mMed);
                assertEquals(AppConstants.ALERT_SEVERITY_MEDIUM, aMed.getSeverity());

                // Case 3: None (>= 95)
                Measurement mNone = createMockMeasurement("o2", 95.0);
                assertNull(method.invoke(seeder, mNone));
        }

        @Test
        @DisplayName("Logic - Measurement Generation (Age & Name Conditions)")
        void seedMeasurements_Logic() throws Exception {
                // Mock Patients
                Patient oldMarie = new Patient();
                oldMarie.setId("p1");
                oldMarie.setName("Marie Curie");
                oldMarie.setAge(70);
                Patient youngJohn = new Patient();
                youngJohn.setId("p2");
                youngJohn.setName("John Doe");
                youngJohn.setAge(30);

                List<Patient> patients = List.of(oldMarie, youngJohn);

                // Setup method
                Method method = DataSeeder.class.getDeclaredMethod("seedMeasurements", List.class);
                method.setAccessible(true);
                when(measurementRepository.count()).thenReturn(0L);
                when(measurementRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

                // Execute
                List<Measurement> results = (List<Measurement>) method.invoke(seeder, patients);

                // Verify "Marie" logic (Temp > 38.2 vs 36.8)
                boolean hasHighTemp = results.stream().anyMatch(m -> m.getPatientId().equals("p1") &&
                                m.getType().equals(AppConstants.MEASUREMENT_TYPE_TEMPERATURE) &&
                                m.getValue() == 38.2);
                assertTrue(hasHighTemp, "Should generate 38.2 temp for 'Marie'");

                // Verify Age > 60 logic (HR 95.0)
                boolean hasHighHR = results.stream().anyMatch(m -> m.getPatientId().equals("p1") &&
                                m.getType().equals(AppConstants.MEASUREMENT_TYPE_HEART_RATE) &&
                                m.getValue() == 95.0);
                assertTrue(hasHighHR, "Should generate 95.0 HR for Age > 60");

                // Verify Age <= 60 logic (HR 78.0)
                boolean hasNormalHR = results.stream().anyMatch(m -> m.getPatientId().equals("p2") &&
                                m.getType().equals(AppConstants.MEASUREMENT_TYPE_HEART_RATE) &&
                                m.getValue() == 78.0);
                assertTrue(hasNormalHR, "Should generate 78.0 HR for Age <= 60");

                // Verify Age > 65 logic (Oxygen 92.0 vs 98.0)
                boolean hasLowO2 = results.stream().anyMatch(m -> m.getPatientId().equals("p1") &&
                                m.getType().equals(AppConstants.MEASUREMENT_TYPE_OXYGEN_SATURATION) &&
                                m.getValue() == 92.0);
                assertTrue(hasLowO2, "Should generate 92.0 O2 for Age > 65");
        }

        @Test
        @DisplayName("Dispatch - checkMeasurementForAlert")
        void checkMeasurementForAlert_Dispatch() throws Exception {
                Method method = DataSeeder.class.getDeclaredMethod("checkMeasurementForAlert", Measurement.class);
                method.setAccessible(true);

                // Verify dispatch to types
                assertNull(method.invoke(seeder, createMockMeasurement("unknown", 100.0)));
                assertNotNull(method.invoke(seeder,
                                createMockMeasurement(AppConstants.MEASUREMENT_TYPE_HEART_RATE, 101.0)));
                assertNotNull(method.invoke(seeder,
                                createMockMeasurement(AppConstants.MEASUREMENT_TYPE_BLOOD_PRESSURE, 161.0)));
        }

        // --- Standard Flow Tests ---

        @Test
        @DisplayName("Run - Empty DB - Should Seed All")
        void run_EmptyDB() throws Exception {
                when(doctorRepository.count()).thenReturn(0L);
                when(doctorRepository.saveAll(any())).thenReturn(List.of(new Doctor(), new Doctor())); // need 2 docs

                when(patientRepository.count()).thenReturn(0L);
                when(patientRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0)); // return list

                when(measurementRepository.count()).thenReturn(0L);
                when(measurementRepository.saveAll(any())).thenReturn(Collections.emptyList());

                when(alertRepository.count()).thenReturn(0L);

                seeder.run();

                verify(doctorRepository).saveAll(any());
                verify(patientRepository).saveAll(any());
                verify(measurementRepository).saveAll(any());
                verify(alertRepository).saveAll(any());
        }

        @Test
        @DisplayName("Run - Full DB - Should Skip")
        void run_FullDB() throws Exception {
                when(doctorRepository.count()).thenReturn(5L);
                when(doctorRepository.findAll()).thenReturn(Collections.emptyList());
                when(patientRepository.count()).thenReturn(5L);
                when(patientRepository.findAll()).thenReturn(Collections.emptyList());
                when(measurementRepository.count()).thenReturn(5L);
                when(measurementRepository.findAll()).thenReturn(Collections.emptyList());
                when(alertRepository.count()).thenReturn(5L);

                seeder.run();

                verify(doctorRepository, never()).saveAll(any());
        }

        // Helper
        private Measurement createMockMeasurement(String type, Double value) {
                Measurement m = new Measurement();
                m.setPatientId("p1");
                m.setId("m1");
                m.setType(type);
                m.setValue(value);
                m.setTimestamp(java.time.LocalDateTime.now());
                return m;
        }
}

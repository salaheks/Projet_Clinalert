package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.repository.AlertRepository;
import com.clinalert.doctortracker.repository.DoctorRepository;
import com.clinalert.doctortracker.repository.MeasurementRepository;
import com.clinalert.doctortracker.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Component - Temporarily disabled to avoid conflict with AuthDataSeeder
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final MeasurementRepository measurementRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AlertRepository alertRepository;

    public DataSeeder(MeasurementRepository measurementRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AlertRepository alertRepository) {
        this.measurementRepository = measurementRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Doctor> doctors = seedDoctors();
        List<Patient> patients = seedPatients(doctors);
        List<Measurement> measurements = seedMeasurements(patients);
        seedAlerts(measurements);
        logSummary();
    }

    private List<Doctor> seedDoctors() {
        if (doctorRepository.count() > 0) {
            return doctorRepository.findAll();
        }

        logger.info("Seeding doctors...");
        Doctor d1 = new Doctor();
        d1.setName("Dr. Gregory House");
        d1.setSpecialty("Diagnostic Medicine");
        d1.setEmail("house@clinalert.com");
        d1.setPhoneNumber("555-0101");

        Doctor d2 = new Doctor();
        d2.setName("Dr. Allison Cameron");
        d2.setSpecialty("Immunology");
        d2.setEmail("cameron@clinalert.com");
        d2.setPhoneNumber("555-0102");

        List<Doctor> doctors = doctorRepository.saveAll(new ArrayList<>(List.of(d1, d2)));
        logger.info("✓ Doctors seeded: {}", doctors.size());
        return doctors;
    }

    private List<Patient> seedPatients(List<Doctor> doctors) {
        if (patientRepository.count() > 0) {
            return patientRepository.findAll();
        }

        logger.info("Seeding patients...");
        String doctor1Id = doctors.get(0).getId();
        String doctor2Id = doctors.get(1).getId();

        Patient p1 = createPatient("Jean Dupont", 45, "M", doctor1Id, AppConstants.STATUS_ACTIVE);
        Patient p2 = createPatient("Marie Martin", 62, "F", doctor1Id, AppConstants.STATUS_ACTIVE);
        Patient p3 = createPatient("Pierre Bernard", 38, "M", doctor2Id, AppConstants.STATUS_TRANSFERRED);
        Patient p4 = createPatient("Sophie Dubois", 55, "F", doctor2Id, AppConstants.STATUS_ACTIVE);
        Patient p5 = createPatient("Luc Moreau", 71, "M", doctor1Id, AppConstants.STATUS_DISCHARGED);

        List<Patient> patients = patientRepository.saveAll(new ArrayList<>(List.of(p1, p2, p3, p4, p5)));
        logger.info("✓ Patients seeded: {}", patients.size());
        return patients;
    }

    private Patient createPatient(String name, int age, String gender, String doctorId, String status) {
        Patient p = new Patient();
        p.setName(name);
        p.setAge(age);
        p.setGender(gender);
        p.setDoctorId(doctorId);
        p.setStatus(status);
        return p;
    }

    private List<Measurement> seedMeasurements(List<Patient> patients) {
        if (measurementRepository.count() > 0) {
            return measurementRepository.findAll();
        }

        logger.info("Seeding measurements...");
        List<Measurement> measurements = new ArrayList<>();

        for (Patient patient : patients) {
            String patientId = patient.getId();

            // Heart rate
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_HEART_RATE, 72.0, 30));
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_HEART_RATE, 75.0, 25));
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_HEART_RATE,
                    patient.getAge() > 60 ? 95.0 : 78.0, 20));

            // Temperature
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_TEMPERATURE, 36.6, 15));
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_TEMPERATURE,
                    patient.getName().contains("Marie") ? 38.2 : 36.8, 10));

            // Blood pressure
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_BLOOD_PRESSURE,
                    patient.getAge() > 60 ? 145.0 : 120.0, 8));

            // Oxygen saturation
            measurements.add(createMeasurement(patientId, AppConstants.MEASUREMENT_TYPE_OXYGEN_SATURATION,
                    patient.getAge() > 65 ? 92.0 : 98.0, 5));
        }

        measurements = measurementRepository.saveAll(measurements);
        logger.info("✓ Measurements seeded: {}", measurements.size());
        return measurements;
    }

    private void seedAlerts(List<Measurement> measurements) {
        if (alertRepository.count() > 0) {
            return;
        }

        logger.info("Seeding alerts...");
        List<Alert> alerts = new ArrayList<>();

        for (Measurement m : measurements) {
            Alert alert = checkMeasurementForAlert(m);
            if (alert != null) {
                alerts.add(alert);
            }
        }

        alertRepository.saveAll(alerts);
        logger.info("✓ Alerts seeded: {}", alerts.size());
    }

    private Alert checkMeasurementForAlert(Measurement m) {
        if (m.getType().equals(AppConstants.MEASUREMENT_TYPE_HEART_RATE)) {
            return checkHeartRateAlert(m);
        } else if (m.getType().equals(AppConstants.MEASUREMENT_TYPE_TEMPERATURE)) {
            return checkTemperatureAlert(m);
        } else if (m.getType().equals(AppConstants.MEASUREMENT_TYPE_BLOOD_PRESSURE)) {
            return checkBloodPressureAlert(m);
        } else if (m.getType().equals(AppConstants.MEASUREMENT_TYPE_OXYGEN_SATURATION)) {
            return checkOxygenAlert(m);
        }
        return null;
    }

    private Alert checkHeartRateAlert(Measurement m) {
        if (m.getValue() > 90) {
            return createAlert(m.getPatientId(), m.getId(),
                    "Rythme cardiaque élevé détecté: " + m.getValue() + " bpm",
                    m.getValue() > 100 ? AppConstants.ALERT_SEVERITY_HIGH : AppConstants.ALERT_SEVERITY_MEDIUM);
        }
        return null;
    }

    private Alert checkTemperatureAlert(Measurement m) {
        if (m.getValue() > 37.5) {
            return createAlert(m.getPatientId(), m.getId(),
                    "Température élevée détectée: " + m.getValue() + "°C",
                    m.getValue() > 38.0 ? AppConstants.ALERT_SEVERITY_HIGH : AppConstants.ALERT_SEVERITY_MEDIUM);
        }
        return null;
    }

    private Alert checkBloodPressureAlert(Measurement m) {
        if (m.getValue() > 140) {
            return createAlert(m.getPatientId(), m.getId(),
                    "Pression artérielle élevée: " + m.getValue() + " mmHg",
                    m.getValue() > 160 ? AppConstants.ALERT_SEVERITY_CRITICAL : AppConstants.ALERT_SEVERITY_HIGH);
        }
        return null;
    }

    private Alert checkOxygenAlert(Measurement m) {
        if (m.getValue() < 95) {
            return createAlert(m.getPatientId(), m.getId(),
                    "Saturation en oxygène faible: " + m.getValue() + "%",
                    m.getValue() < 90 ? AppConstants.ALERT_SEVERITY_CRITICAL : AppConstants.ALERT_SEVERITY_MEDIUM);
        }
        return null;
    }

    private void logSummary() {
        logger.info("=================================");
        logger.info("Database seeding completed!");
        logger.info("Doctors: {}", doctorRepository.count());
        logger.info("Patients: {}", patientRepository.count());
        logger.info("Measurements: {}", measurementRepository.count());
        logger.info("Alerts: {}", alertRepository.count());
        logger.info("=================================");
    }

    private Measurement createMeasurement(String patientId, String type, Double value, int minutesAgo) {
        Measurement m = new Measurement();
        m.setPatientId(patientId);
        m.setDeviceId("device-001");
        m.setType(type);
        m.setValue(value);
        m.setTimestamp(LocalDateTime.now().minusMinutes(minutesAgo));
        m.setReceivedAt(LocalDateTime.now().minusMinutes(minutesAgo));
        return m;
    }

    private Alert createAlert(String patientId, String measurementId, String message, String severity) {
        Alert alert = new Alert();
        alert.setPatientId(patientId);
        alert.setMeasurementId(measurementId);
        alert.setMessage(message);
        alert.setSeverity(severity);
        return alert;
    }
}

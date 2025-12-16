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

// @Component - Temporarily disabled to avoid conflict with AuthDataSeeder
public class DataSeeder implements CommandLineRunner {

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
        // Seed Doctors
        List<Doctor> doctors = new ArrayList<>();
        if (doctorRepository.count() == 0) {
            System.out.println("Seeding doctors...");

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

            doctors = doctorRepository.saveAll(new ArrayList<>(List.of(d1, d2)));
            System.out.println("✓ Doctors seeded: " + doctors.size());
        } else {
            doctors = doctorRepository.findAll();
        }

        // Seed Patients
        List<Patient> patients = new ArrayList<>();
        if (patientRepository.count() == 0) {
            System.out.println("Seeding patients...");

            String doctor1Id = doctors.get(0).getId();
            String doctor2Id = doctors.get(1).getId();

            Patient p1 = new Patient();
            p1.setName("Jean Dupont");
            p1.setAge(45);
            p1.setGender("M");
            p1.setDoctorId(doctor1Id);
            p1.setStatus("active");

            Patient p2 = new Patient();
            p2.setName("Marie Martin");
            p2.setAge(62);
            p2.setGender("F");
            p2.setDoctorId(doctor1Id);
            p2.setStatus("active");

            Patient p3 = new Patient();
            p3.setName("Pierre Bernard");
            p3.setAge(38);
            p3.setGender("M");
            p3.setDoctorId(doctor2Id);
            p3.setStatus("transferred");

            Patient p4 = new Patient();
            p4.setName("Sophie Dubois");
            p4.setAge(55);
            p4.setGender("F");
            p4.setDoctorId(doctor2Id);
            p4.setStatus("active");

            Patient p5 = new Patient();
            p5.setName("Luc Moreau");
            p5.setAge(71);
            p5.setGender("M");
            p5.setDoctorId(doctor1Id);
            p5.setStatus("discharged");

            patients = patientRepository.saveAll(new ArrayList<>(List.of(p1, p2, p3, p4, p5)));
            System.out.println("✓ Patients seeded: " + patients.size());
        } else {
            patients = patientRepository.findAll();
        }

        // Seed Measurements
        List<Measurement> measurements = new ArrayList<>();
        if (measurementRepository.count() == 0) {
            System.out.println("Seeding measurements...");

            for (Patient patient : patients) {
                String patientId = patient.getId();

                // Heart rate measurements (normal and some abnormal)
                measurements.add(createMeasurement(patientId, "heart_rate", 72.0, 30));
                measurements.add(createMeasurement(patientId, "heart_rate", 75.0, 25));
                measurements.add(createMeasurement(patientId, "heart_rate",
                        patient.getAge() > 60 ? 95.0 : 78.0, 20));

                // Temperature
                measurements.add(createMeasurement(patientId, "temperature", 36.6, 15));
                measurements.add(createMeasurement(patientId, "temperature",
                        patient.getName().contains("Marie") ? 38.2 : 36.8, 10));

                // Blood pressure (systolic)
                measurements.add(createMeasurement(patientId, "blood_pressure_systolic",
                        patient.getAge() > 60 ? 145.0 : 120.0, 8));

                // Oxygen saturation
                measurements.add(createMeasurement(patientId, "oxygen_saturation",
                        patient.getAge() > 65 ? 92.0 : 98.0, 5));
            }

            measurements = measurementRepository.saveAll(measurements);
            System.out.println("✓ Measurements seeded: " + measurements.size());
        } else {
            measurements = measurementRepository.findAll();
        }

        // Seed Alerts
        if (alertRepository.count() == 0) {
            System.out.println("Seeding alerts...");
            List<Alert> alerts = new ArrayList<>();

            // Create alerts for abnormal measurements
            for (Measurement m : measurements) {
                Alert alert = null;

                if (m.getType().equals("heart_rate") && m.getValue() > 90) {
                    alert = createAlert(m.getPatientId(), m.getId(),
                            "Rythme cardiaque élevé détecté: " + m.getValue() + " bpm",
                            m.getValue() > 100 ? "HIGH" : "MEDIUM");
                } else if (m.getType().equals("temperature") && m.getValue() > 37.5) {
                    alert = createAlert(m.getPatientId(), m.getId(),
                            "Température élevée détectée: " + m.getValue() + "°C",
                            m.getValue() > 38.0 ? "HIGH" : "MEDIUM");
                } else if (m.getType().equals("blood_pressure_systolic") && m.getValue() > 140) {
                    alert = createAlert(m.getPatientId(), m.getId(),
                            "Pression artérielle élevée: " + m.getValue() + " mmHg",
                            m.getValue() > 160 ? "CRITICAL" : "HIGH");
                } else if (m.getType().equals("oxygen_saturation") && m.getValue() < 95) {
                    alert = createAlert(m.getPatientId(), m.getId(),
                            "Saturation en oxygène faible: " + m.getValue() + "%",
                            m.getValue() < 90 ? "CRITICAL" : "MEDIUM");
                }

                if (alert != null) {
                    alerts.add(alert);
                }
            }

            alertRepository.saveAll(alerts);
            System.out.println("✓ Alerts seeded: " + alerts.size());
        }

        System.out.println("=================================");
        System.out.println("Database seeding completed!");
        System.out.println("Doctors: " + doctorRepository.count());
        System.out.println("Patients: " + patientRepository.count());
        System.out.println("Measurements: " + measurementRepository.count());
        System.out.println("Alerts: " + alertRepository.count());
        System.out.println("=================================");
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

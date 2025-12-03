package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.repository.MeasurementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MeasurementRepository measurementRepository;

    public DataSeeder(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @Override
    @SuppressWarnings("null")
    public void run(String... args) throws Exception {
        if (measurementRepository.count() == 0) {
            System.out.println("Seeding database with sample measurements...");

            Measurement m1 = new Measurement();
            m1.setPatientId("patient-123");
            m1.setDeviceId("device-001");
            m1.setType("heart_rate");
            m1.setValue(72.0);
            m1.setTimestamp(LocalDateTime.now().minusMinutes(10));
            m1.setReceivedAt(LocalDateTime.now());

            Measurement m2 = new Measurement();
            m2.setPatientId("patient-123");
            m2.setDeviceId("device-001");
            m2.setType("heart_rate");
            m2.setValue(75.0);
            m2.setTimestamp(LocalDateTime.now().minusMinutes(5));
            m2.setReceivedAt(LocalDateTime.now());

            Measurement m3 = new Measurement();
            m3.setPatientId("patient-123");
            m3.setDeviceId("device-001");
            m3.setType("temperature");
            m3.setValue(36.6);
            m3.setTimestamp(LocalDateTime.now().minusMinutes(2));
            m3.setReceivedAt(LocalDateTime.now());

            measurementRepository.saveAll(Arrays.asList(m1, m2, m3));
            System.out.println("Database seeded!");
        }
    }
}

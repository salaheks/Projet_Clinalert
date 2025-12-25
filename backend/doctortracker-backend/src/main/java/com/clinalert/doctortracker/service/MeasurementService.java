package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.model.Measurement;
import com.clinalert.doctortracker.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;

    private final AlertService alertService;

    private final AnomalyDetectionService anomalyDetectionService;

    @SuppressWarnings("null")
    public List<Measurement> saveMeasurements(List<Measurement> measurements) {
        List<Measurement> saved = measurementRepository.saveAll(measurements);

        // Check for alerts
        for (Measurement m : saved) {
            checkAndCreateAlert(m);
        }

        return saved;
    }

    public List<Measurement> getHistory(String patientId) {
        return measurementRepository.findByPatientId(patientId);
    }

    private void checkAndCreateAlert(Measurement m) {
        // AI Anomaly Detection
        if (anomalyDetectionService.isAnomaly(m)) {
            createAlert(m, "Abnormal trend detected for " + m.getType(), "WARNING");
        }

        if ("Heart Rate".equalsIgnoreCase(m.getType())) {
            if (m.getValue() > 100) {
                createAlert(m, "High Heart Rate detected: " + m.getValue(), "HIGH");
            } else if (m.getValue() < 50) {
                createAlert(m, "Low Heart Rate detected: " + m.getValue(), "MEDIUM");
            }
        } else if ("SpO2".equalsIgnoreCase(m.getType()) && m.getValue() < 90) {
            createAlert(m, "Low SpO2 detected: " + m.getValue(), "CRITICAL");
        }
    }

    private void createAlert(Measurement m, String message, String severity) {
        Alert alert = new Alert();
        alert.setPatientId(m.getPatientId());
        alert.setMeasurementId(m.getId());
        alert.setMessage(message);
        alert.setSeverity(severity);
        alertService.createAlert(alert);
    }
}

package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscalationService {

    @Autowired
    private AlertRepository alertRepository;

    // Run every minute
    @Scheduled(fixedRate = 60000)
    public void checkAndEscalateAlerts() {
        LocalDateTime thresholdTime = LocalDateTime.now().minusMinutes(15);

        // Find active alerts that are older than 15 minutes and not yet acknowledged
        // (assuming 'active' means unacknowledged)
        // In a real scenario, we would check a specific 'acknowledged' flag.
        // For now, we assume we are looking for MEDIUM severity alerts to upgrade to
        // HIGH.

        List<Alert> pendingAlerts = alertRepository.findBySeverityAndTimestampBefore("MEDIUM", thresholdTime);

        for (Alert alert : pendingAlerts) {
            // Escalate to HIGH
            alert.setSeverity("HIGH");
            alert.setMessage("[ESCALATED] " + alert.getMessage());
            alertRepository.save(alert);

            // Here we would also trigger a notification to a broader team or supervisor
            System.out.println("Escalated alert " + alert.getId() + " for patient " + alert.getPatientId());
        }
    }
}

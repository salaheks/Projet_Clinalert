package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public List<Alert> getAlertsByPatientId(String patientId) {
        return alertRepository.findByPatientId(patientId);
    }

    public List<Alert> getUnreadAlerts() {
        return alertRepository.findByIsReadFalse();
    }

    @SuppressWarnings("null")
    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    @SuppressWarnings("null")
    public void markAsRead(String id) {
        Optional<Alert> alert = alertRepository.findById(id);
        if (alert.isPresent()) {
            Alert a = alert.get();
            a.setRead(true);
            alertRepository.save(a);
        }
    }
}

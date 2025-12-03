package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    @GetMapping("/patient/{patientId}")
    public List<Alert> getAlertsByPatient(@PathVariable String patientId) {
        return alertService.getAlertsByPatientId(patientId);
    }

    @GetMapping("/unread")
    public List<Alert> getUnreadAlerts() {
        return alertService.getUnreadAlerts();
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        alertService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}

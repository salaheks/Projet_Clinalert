package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.model.DailyHealthSummary;
import com.clinalert.doctortracker.model.HealthData;
import com.clinalert.doctortracker.model.SmartWatchDevice;
import com.clinalert.doctortracker.service.SmartWatchHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smartwatch")
public class SmartWatchController {

    @Autowired
    private SmartWatchHealthService smartWatchHealthService;

    // ==================== Device Endpoints ====================

    @PostMapping("/devices")
    public ResponseEntity<SmartWatchDevice> registerDevice(@RequestBody SmartWatchDevice device) {
        SmartWatchDevice registered = smartWatchHealthService.registerDevice(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @GetMapping("/devices/{patientId}")
    public ResponseEntity<List<SmartWatchDevice>> getPatientDevices(@PathVariable String patientId) {
        List<SmartWatchDevice> devices = smartWatchHealthService.getPatientDevices(patientId);
        return ResponseEntity.ok(devices);
    }

    @GetMapping("/devices/{patientId}/active")
    public ResponseEntity<List<SmartWatchDevice>> getActiveDevices(@PathVariable String patientId) {
        List<SmartWatchDevice> devices = smartWatchHealthService.getActiveDevices(patientId);
        return ResponseEntity.ok(devices);
    }

    @PutMapping("/devices/{deviceId}/deactivate")
    public ResponseEntity<Void> deactivateDevice(@PathVariable String deviceId) {
        smartWatchHealthService.deactivateDevice(deviceId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/devices/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String deviceId) {
        smartWatchHealthService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/devices/{deviceId}/ping")
    public ResponseEntity<Void> pingDevice(@PathVariable String deviceId) {
        smartWatchHealthService.updateDeviceLastConnected(deviceId);
        return ResponseEntity.ok().build();
    }

    // ==================== Health Data Endpoints ====================

    @PostMapping("/health-data")
    public ResponseEntity<?> submitHealthData(@RequestBody List<HealthData> healthDataList) {
        if (healthDataList == null || healthDataList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No health data provided"));
        }

        List<HealthData> saved = smartWatchHealthService.saveHealthData(healthDataList);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Health data saved successfully",
                "count", saved.size()));
    }

    @PostMapping("/health-data/single")
    public ResponseEntity<HealthData> submitSingleHealthData(@RequestBody HealthData healthData) {
        HealthData saved = smartWatchHealthService.saveHealthData(healthData);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/health-data/{patientId}")
    public ResponseEntity<List<HealthData>> getPatientHealthData(@PathVariable String patientId) {
        List<HealthData> data = smartWatchHealthService.getPatientHealthData(patientId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/health-data/{patientId}/range")
    public ResponseEntity<List<HealthData>> getPatientHealthDataRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HealthData> data = smartWatchHealthService.getPatientHealthDataBetween(patientId, start, end);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/health-data/{patientId}/heart-rate")
    public ResponseEntity<List<HealthData>> getHeartRateHistory(@PathVariable String patientId) {
        return ResponseEntity.ok(smartWatchHealthService.getHeartRateHistory(patientId));
    }

    @GetMapping("/health-data/{patientId}/steps")
    public ResponseEntity<List<HealthData>> getStepsHistory(@PathVariable String patientId) {
        return ResponseEntity.ok(smartWatchHealthService.getStepsHistory(patientId));
    }

    @GetMapping("/health-data/{patientId}/spo2")
    public ResponseEntity<List<HealthData>> getSpO2History(@PathVariable String patientId) {
        return ResponseEntity.ok(smartWatchHealthService.getSpO2History(patientId));
    }

    @GetMapping("/health-data/{patientId}/sleep")
    public ResponseEntity<List<HealthData>> getSleepHistory(@PathVariable String patientId) {
        return ResponseEntity.ok(smartWatchHealthService.getSleepHistory(patientId));
    }

    @GetMapping("/health-data/{patientId}/stats")
    public ResponseEntity<SmartWatchHealthService.HealthDataStats> getPatientStats(@PathVariable String patientId) {
        return ResponseEntity.ok(smartWatchHealthService.getPatientStats(patientId));
    }

    // ==================== Daily Summary Endpoints ====================

    @PostMapping("/daily-summary/{patientId}/generate")
    public ResponseEntity<?> generateDailySummary(
            @PathVariable String patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        DailyHealthSummary summary = smartWatchHealthService.generateDailySummary(patientId, targetDate);

        if (summary == null) {
            return ResponseEntity.ok(Map.of("message", "No data available for the specified date"));
        }

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/daily-summary/{patientId}")
    public ResponseEntity<List<DailyHealthSummary>> getRecentDailySummaries(@PathVariable String patientId) {
        List<DailyHealthSummary> summaries = smartWatchHealthService.getRecentDailySummaries(patientId);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/daily-summary/{patientId}/{date}")
    public ResponseEntity<?> getDailySummary(
            @PathVariable String patientId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return smartWatchHealthService.getDailySummary(patientId, date)
                .map(summary -> ResponseEntity.ok((Object) summary))
                .orElse(ResponseEntity.ok(Map.of("message", "No summary available for the specified date")));
    }

    @GetMapping("/daily-summary/{patientId}/range")
    public ResponseEntity<List<DailyHealthSummary>> getDailySummariesRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<DailyHealthSummary> summaries = smartWatchHealthService.getDailySummariesBetween(patientId, start, end);
        return ResponseEntity.ok(summaries);
    }
}

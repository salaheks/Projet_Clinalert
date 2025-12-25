package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.DailyHealthSummary;
import com.clinalert.doctortracker.model.HealthData;
import com.clinalert.doctortracker.model.SmartWatchDevice;
import com.clinalert.doctortracker.model.Alert;
import com.clinalert.doctortracker.repository.DailyHealthSummaryRepository;
import com.clinalert.doctortracker.repository.HealthDataRepository;
import com.clinalert.doctortracker.repository.SmartWatchDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SmartWatchHealthService {

    private final SmartWatchDeviceRepository deviceRepository;

    private final HealthDataRepository healthDataRepository;

    private final DailyHealthSummaryRepository dailySummaryRepository;

    private final AlertService alertService;

    // ==================== Device Management ====================

    public SmartWatchDevice registerDevice(SmartWatchDevice device) {
        // Check if device already exists for this patient
        Optional<SmartWatchDevice> existing = deviceRepository
                .findByPatientIdAndDeviceAddress(device.getPatientId(), device.getDeviceAddress());

        if (existing.isPresent()) {
            // Update existing device
            SmartWatchDevice existingDevice = existing.get();
            existingDevice.setDeviceName(device.getDeviceName());
            existingDevice.setDeviceType(device.getDeviceType());
            existingDevice.setIsActive(true);
            existingDevice.setLastConnected(LocalDateTime.now());
            return deviceRepository.save(existingDevice);
        }

        device.setLastConnected(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    public List<SmartWatchDevice> getPatientDevices(String patientId) {
        return deviceRepository.findByPatientId(patientId);
    }

    public List<SmartWatchDevice> getActiveDevices(String patientId) {
        return deviceRepository.findByPatientIdAndIsActiveTrue(patientId);
    }

    public void deactivateDevice(String deviceId) {
        Objects.requireNonNull(deviceId, com.clinalert.doctortracker.util.AppConstants.ERROR_DEVICE_ID_NULL);
        deviceRepository.findById(deviceId).ifPresent(device -> {
            device.setIsActive(false);
            deviceRepository.save(device);
        });
    }

    public void deleteDevice(String deviceId) {
        Objects.requireNonNull(deviceId, com.clinalert.doctortracker.util.AppConstants.ERROR_DEVICE_ID_NULL);
        deviceRepository.deleteById(deviceId);
    }

    public void updateDeviceLastConnected(String deviceId) {
        Objects.requireNonNull(deviceId, com.clinalert.doctortracker.util.AppConstants.ERROR_DEVICE_ID_NULL);
        deviceRepository.findById(deviceId).ifPresent(device -> {
            device.setLastConnected(LocalDateTime.now());
            deviceRepository.save(device);
        });
    }

    // ==================== Health Data Management ====================

    @Transactional
    public List<HealthData> saveHealthData(List<HealthData> healthDataList) {
        Objects.requireNonNull(healthDataList, "healthDataList must not be null");
        List<HealthData> saved = healthDataRepository.saveAll(healthDataList);

        // Check for anomalies and create alerts
        for (HealthData data : saved) {
            checkAndCreateAlerts(data);
        }

        return saved;
    }

    public HealthData saveHealthData(HealthData healthData) {
        Objects.requireNonNull(healthData, "healthData must not be null");
        HealthData saved = healthDataRepository.save(healthData);
        checkAndCreateAlerts(saved);
        return saved;
    }

    public List<HealthData> getPatientHealthData(String patientId) {
        return healthDataRepository.findTop50ByPatientIdOrderByTimestampDesc(patientId);
    }

    public List<HealthData> getPatientHealthDataBetween(String patientId, LocalDateTime start, LocalDateTime end) {
        return healthDataRepository.findByPatientIdAndTimestampBetweenOrderByTimestampAsc(patientId, start, end);
    }

    public List<HealthData> getHeartRateHistory(String patientId) {
        return healthDataRepository.findHeartRateDataByPatientId(patientId);
    }

    public List<HealthData> getStepsHistory(String patientId) {
        return healthDataRepository.findStepsDataByPatientId(patientId);
    }

    public List<HealthData> getSpO2History(String patientId) {
        return healthDataRepository.findSpO2DataByPatientId(patientId);
    }

    public List<HealthData> getSleepHistory(String patientId) {
        return healthDataRepository.findSleepDataByPatientId(patientId);
    }

    // ==================== Daily Summary Management ====================

    @Transactional
    public DailyHealthSummary generateDailySummary(String patientId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<HealthData> dayData = healthDataRepository
                .findByPatientIdAndTimestampBetweenOrderByTimestampAsc(patientId, startOfDay, endOfDay);

        if (dayData.isEmpty()) {
            return null;
        }

        // Check for existing summary
        DailyHealthSummary summary = dailySummaryRepository
                .findByPatientIdAndDate(patientId, date)
                .orElse(new DailyHealthSummary());

        summary.setPatientId(patientId);
        summary.setDate(date);
        summary.setDataPointsCount(dayData.size());

        // Calculate heart rate stats
        List<Integer> heartRates = dayData.stream()
                .filter(d -> d.getHeartRate() != null)
                .map(HealthData::getHeartRate)
                .toList();

        if (!heartRates.isEmpty()) {
            summary.setAvgHeartRate(heartRates.stream().mapToInt(Integer::intValue).average().orElse(0));
            summary.setMinHeartRate(heartRates.stream().mapToInt(Integer::intValue).min().orElse(0));
            summary.setMaxHeartRate(heartRates.stream().mapToInt(Integer::intValue).max().orElse(0));
        }

        // Calculate steps total
        int totalSteps = dayData.stream()
                .filter(d -> d.getSteps() != null)
                .mapToInt(HealthData::getSteps)
                .sum();
        summary.setTotalSteps(totalSteps);

        // Calculate sleep total
        int totalSleep = dayData.stream()
                .filter(d -> d.getSleepMinutes() != null)
                .mapToInt(HealthData::getSleepMinutes)
                .sum();
        summary.setTotalSleepMinutes(totalSleep);

        // Calculate SpO2 average
        List<Double> spO2Values = dayData.stream()
                .filter(d -> d.getSpO2() != null)
                .map(HealthData::getSpO2)
                .toList();

        if (!spO2Values.isEmpty()) {
            summary.setAvgSpO2(spO2Values.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            summary.setMinSpO2(spO2Values.stream().mapToDouble(Double::doubleValue).min().orElse(0));
        }

        // Calculate blood pressure averages
        List<Integer> systolicValues = dayData.stream()
                .filter(d -> d.getBloodPressureSystolic() != null)
                .map(HealthData::getBloodPressureSystolic)
                .toList();

        if (!systolicValues.isEmpty()) {
            summary.setAvgSystolic(systolicValues.stream().mapToInt(Integer::intValue).average().orElse(0));
        }

        List<Integer> diastolicValues = dayData.stream()
                .filter(d -> d.getBloodPressureDiastolic() != null)
                .map(HealthData::getBloodPressureDiastolic)
                .toList();

        if (!diastolicValues.isEmpty()) {
            summary.setAvgDiastolic(diastolicValues.stream().mapToInt(Integer::intValue).average().orElse(0));
        }

        // Calculate calories and distance
        int totalCalories = dayData.stream()
                .filter(d -> d.getCaloriesBurned() != null)
                .mapToInt(HealthData::getCaloriesBurned)
                .sum();
        summary.setTotalCaloriesBurned(totalCalories);

        double totalDistance = dayData.stream()
                .filter(d -> d.getDistanceMeters() != null)
                .mapToDouble(HealthData::getDistanceMeters)
                .sum();
        summary.setTotalDistanceMeters(totalDistance);

        // Calculate temperature average
        List<Double> tempValues = dayData.stream()
                .filter(d -> d.getTemperature() != null)
                .map(HealthData::getTemperature)
                .toList();

        if (!tempValues.isEmpty()) {
            summary.setAvgTemperature(tempValues.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        }

        return dailySummaryRepository.save(summary);
    }

    public Optional<DailyHealthSummary> getDailySummary(String patientId, LocalDate date) {
        return dailySummaryRepository.findByPatientIdAndDate(patientId, date);
    }

    public List<DailyHealthSummary> getRecentDailySummaries(String patientId) {
        return dailySummaryRepository.findTop30ByPatientIdOrderByDateDesc(patientId);
    }

    public List<DailyHealthSummary> getDailySummariesBetween(String patientId, LocalDate start, LocalDate end) {
        return dailySummaryRepository.findByPatientIdAndDateBetweenOrderByDateAsc(patientId, start, end);
    }

    // ==================== Alert Detection ====================

    private void checkAndCreateAlerts(HealthData data) {
        checkHeartRate(data);
        checkSpO2(data);
        checkBloodPressure(data);
        checkTemperature(data);
    }

    private void checkHeartRate(HealthData data) {
        if (data.getHeartRate() != null) {
            String alertMessage = null;
            String severity = null;

            if (data.getHeartRate() > 150) {
                alertMessage = "Critical Heart Rate: " + data.getHeartRate() + " bpm";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_CRITICAL;
            } else if (data.getHeartRate() > 120) {
                alertMessage = "High Heart Rate: " + data.getHeartRate() + " bpm";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_HIGH;
            } else if (data.getHeartRate() < 50) {
                alertMessage = "Low Heart Rate: " + data.getHeartRate() + " bpm";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_MEDIUM;
            }

            if (alertMessage != null) {
                createAlert(data, alertMessage, severity);
            }
        }
    }

    private void checkSpO2(HealthData data) {
        if (data.getSpO2() != null) {
            String alertMessage = null;
            String severity = null;

            if (data.getSpO2() < 90) {
                alertMessage = "Critical SpO2 Level: " + data.getSpO2() + "%";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_CRITICAL;
            } else if (data.getSpO2() < 94) {
                alertMessage = "Low SpO2 Level: " + data.getSpO2() + "%";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_HIGH;
            }

            if (alertMessage != null) {
                createAlert(data, alertMessage, severity);
            }
        }
    }

    private void checkBloodPressure(HealthData data) {
        if (data.getBloodPressureSystolic() != null) {
            String alertMessage = null;
            String severity = null;

            if (data.getBloodPressureSystolic() > 180) {
                alertMessage = "Hypertensive Crisis: " + data.getBloodPressureSystolic() + "/" +
                        data.getBloodPressureDiastolic() + " mmHg";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_CRITICAL;
            } else if (data.getBloodPressureSystolic() > 140) {
                alertMessage = "High Blood Pressure: " + data.getBloodPressureSystolic() + "/" +
                        data.getBloodPressureDiastolic() + " mmHg";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_HIGH;
            }

            if (alertMessage != null) {
                createAlert(data, alertMessage, severity);
            }
        }
    }

    private void checkTemperature(HealthData data) {
        if (data.getTemperature() != null) {
            String alertMessage = null;
            String severity = null;

            if (data.getTemperature() > 40.0) {
                alertMessage = "Critical Fever: " + data.getTemperature() + "°C";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_CRITICAL;
            } else if (data.getTemperature() > 39.0) {
                alertMessage = "High Fever: " + data.getTemperature() + "°C";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_HIGH;
            } else if (data.getTemperature() < 35.0) {
                alertMessage = "Hypothermia: " + data.getTemperature() + "°C";
                severity = com.clinalert.doctortracker.util.AppConstants.ALERT_SEVERITY_HIGH;
            }

            if (alertMessage != null) {
                createAlert(data, alertMessage, severity);
            }
        }
    }

    private void createAlert(HealthData data, String message, String severity) {
        Alert alert = new Alert();
        alert.setPatientId(data.getPatientId());
        alert.setMessage(message);
        alert.setSeverity(severity);
        alertService.createAlert(alert);
    }

    // ==================== Statistics ====================

    public HealthDataStats getPatientStats(String patientId) {
        List<HealthData> recentData = healthDataRepository.findTop50ByPatientIdOrderByTimestampDesc(patientId);

        HealthDataStats stats = new HealthDataStats();

        if (recentData.isEmpty()) {
            return stats;
        }

        // Get latest values
        HealthData latest = recentData.get(0);
        stats.setLatestHeartRate(latest.getHeartRate());
        stats.setLatestSpO2(latest.getSpO2());
        stats.setLatestSteps(latest.getSteps());
        stats.setLatestTimestamp(latest.getTimestamp());

        // Calculate averages
        stats.setAvgHeartRate(recentData.stream()
                .filter(d -> d.getHeartRate() != null)
                .mapToInt(HealthData::getHeartRate)
                .average()
                .orElse(0));

        stats.setTotalDataPoints(recentData.size());

        return stats;
    }

    // Inner class for stats
    public static class HealthDataStats {
        private Integer latestHeartRate;
        private Double latestSpO2;
        private Integer latestSteps;
        private LocalDateTime latestTimestamp;
        private Double avgHeartRate;
        private Integer totalDataPoints;

        // Getters and Setters
        public Integer getLatestHeartRate() {
            return latestHeartRate;
        }

        public void setLatestHeartRate(Integer latestHeartRate) {
            this.latestHeartRate = latestHeartRate;
        }

        public Double getLatestSpO2() {
            return latestSpO2;
        }

        public void setLatestSpO2(Double latestSpO2) {
            this.latestSpO2 = latestSpO2;
        }

        public Integer getLatestSteps() {
            return latestSteps;
        }

        public void setLatestSteps(Integer latestSteps) {
            this.latestSteps = latestSteps;
        }

        public LocalDateTime getLatestTimestamp() {
            return latestTimestamp;
        }

        public void setLatestTimestamp(LocalDateTime latestTimestamp) {
            this.latestTimestamp = latestTimestamp;
        }

        public Double getAvgHeartRate() {
            return avgHeartRate;
        }

        public void setAvgHeartRate(Double avgHeartRate) {
            this.avgHeartRate = avgHeartRate;
        }

        public Integer getTotalDataPoints() {
            return totalDataPoints;
        }

        public void setTotalDataPoints(Integer totalDataPoints) {
            this.totalDataPoints = totalDataPoints;
        }
    }
}

package com.clinalert.doctortracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_health_summaries", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "patient_id", "summary_date" })
})
public class DailyHealthSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate date;

    // Heart Rate Stats
    @Column(name = "avg_heart_rate")
    private Double avgHeartRate;

    @Column(name = "min_heart_rate")
    private Integer minHeartRate;

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    @Column(name = "resting_heart_rate")
    private Integer restingHeartRate;

    // Steps & Activity
    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "total_distance_meters")
    private Double totalDistanceMeters;

    @Column(name = "total_calories_burned")
    private Integer totalCaloriesBurned;

    @Column(name = "active_minutes")
    private Integer activeMinutes;

    // Sleep Stats
    @Column(name = "total_sleep_minutes")
    private Integer totalSleepMinutes;

    @Column(name = "deep_sleep_minutes")
    private Integer deepSleepMinutes;

    @Column(name = "light_sleep_minutes")
    private Integer lightSleepMinutes;

    @Column(name = "rem_sleep_minutes")
    private Integer remSleepMinutes;

    // SpO2 Stats
    @Column(name = "avg_spo2")
    private Double avgSpO2;

    @Column(name = "min_spo2")
    private Double minSpO2;

    // Blood Pressure Stats
    @Column(name = "avg_systolic")
    private Double avgSystolic;

    @Column(name = "avg_diastolic")
    private Double avgDiastolic;

    // Temperature Stats
    @Column(name = "avg_temperature")
    private Double avgTemperature;

    @Column(name = "data_points_count")
    private Integer dataPointsCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getAvgHeartRate() {
        return avgHeartRate;
    }

    public void setAvgHeartRate(Double avgHeartRate) {
        this.avgHeartRate = avgHeartRate;
    }

    public Integer getMinHeartRate() {
        return minHeartRate;
    }

    public void setMinHeartRate(Integer minHeartRate) {
        this.minHeartRate = minHeartRate;
    }

    public Integer getMaxHeartRate() {
        return maxHeartRate;
    }

    public void setMaxHeartRate(Integer maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public Integer getRestingHeartRate() {
        return restingHeartRate;
    }

    public void setRestingHeartRate(Integer restingHeartRate) {
        this.restingHeartRate = restingHeartRate;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public Double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public void setTotalDistanceMeters(Double totalDistanceMeters) {
        this.totalDistanceMeters = totalDistanceMeters;
    }

    public Integer getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public void setTotalCaloriesBurned(Integer totalCaloriesBurned) {
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public Integer getActiveMinutes() {
        return activeMinutes;
    }

    public void setActiveMinutes(Integer activeMinutes) {
        this.activeMinutes = activeMinutes;
    }

    public Integer getTotalSleepMinutes() {
        return totalSleepMinutes;
    }

    public void setTotalSleepMinutes(Integer totalSleepMinutes) {
        this.totalSleepMinutes = totalSleepMinutes;
    }

    public Integer getDeepSleepMinutes() {
        return deepSleepMinutes;
    }

    public void setDeepSleepMinutes(Integer deepSleepMinutes) {
        this.deepSleepMinutes = deepSleepMinutes;
    }

    public Integer getLightSleepMinutes() {
        return lightSleepMinutes;
    }

    public void setLightSleepMinutes(Integer lightSleepMinutes) {
        this.lightSleepMinutes = lightSleepMinutes;
    }

    public Integer getRemSleepMinutes() {
        return remSleepMinutes;
    }

    public void setRemSleepMinutes(Integer remSleepMinutes) {
        this.remSleepMinutes = remSleepMinutes;
    }

    public Double getAvgSpO2() {
        return avgSpO2;
    }

    public void setAvgSpO2(Double avgSpO2) {
        this.avgSpO2 = avgSpO2;
    }

    public Double getMinSpO2() {
        return minSpO2;
    }

    public void setMinSpO2(Double minSpO2) {
        this.minSpO2 = minSpO2;
    }

    public Double getAvgSystolic() {
        return avgSystolic;
    }

    public void setAvgSystolic(Double avgSystolic) {
        this.avgSystolic = avgSystolic;
    }

    public Double getAvgDiastolic() {
        return avgDiastolic;
    }

    public void setAvgDiastolic(Double avgDiastolic) {
        this.avgDiastolic = avgDiastolic;
    }

    public Double getAvgTemperature() {
        return avgTemperature;
    }

    public void setAvgTemperature(Double avgTemperature) {
        this.avgTemperature = avgTemperature;
    }

    public Integer getDataPointsCount() {
        return dataPointsCount;
    }

    public void setDataPointsCount(Integer dataPointsCount) {
        this.dataPointsCount = dataPointsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

package com.clinalert.doctortracker.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "smartwatch_devices")
public class SmartWatchDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_address", nullable = false)
    private String deviceAddress;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "last_connected")
    private LocalDateTime lastConnected;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public LocalDateTime getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(LocalDateTime lastConnected) {
        this.lastConnected = lastConnected;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

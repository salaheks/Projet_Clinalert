package com.clinalert.doctortracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "measurements")
public class Measurement {
    @Id
    private String id;

    private String patientId;
    private String deviceId;
    private String type;
    private Double value;
    private LocalDateTime timestamp;
    private String consentId;

    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}

package com.clinalert.doctortracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    private Integer age;
    private String gender;

    @Column(name = "doctor_id")
    private String doctorId;
}

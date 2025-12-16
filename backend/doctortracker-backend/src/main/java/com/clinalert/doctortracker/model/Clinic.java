package com.clinalert.doctortracker.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clinics")
public class Clinic {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    private String address;

    private String phone;

    @Column(name = "doctor_id")
    private String doctorId;

    // Constructors
    public Clinic() {
    }

    public Clinic(String name, String address, String phone, String doctorId) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }
}

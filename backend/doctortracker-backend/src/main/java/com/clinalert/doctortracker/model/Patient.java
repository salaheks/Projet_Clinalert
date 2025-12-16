package com.clinalert.doctortracker.model;

import jakarta.persistence.*;

@Entity
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

    @Column(name = "clinic_id")
    private String clinicId;

    private String status; // active, discharged, transferred

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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getClinicId() {
        return clinicId;
    }

    public void setClinicId(String clinicId) {
        this.clinicId = clinicId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package com.clinalert.doctortracker.dto;

import com.clinalert.doctortracker.model.User;

public class RegisterRequest {
    private String email;
    private String password;
    private User.UserRole role;

    // Optional fields for doctor/patient creation
    private String name;
    private String specialty; // For doctors
    private String phoneNumber; // For doctors
    private Integer age; // For patients
    private String gender; // For patients
    private String doctorId; // For patients

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, User.UserRole role, String name,
            String specialty, String phoneNumber, Integer age, String gender, String doctorId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.specialty = specialty;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.gender = gender;
        this.doctorId = doctorId;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User.UserRole getRole() {
        return role;
    }

    public void setRole(User.UserRole role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
}

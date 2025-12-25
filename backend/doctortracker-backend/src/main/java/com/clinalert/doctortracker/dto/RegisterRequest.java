package com.clinalert.doctortracker.dto;

import com.clinalert.doctortracker.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}

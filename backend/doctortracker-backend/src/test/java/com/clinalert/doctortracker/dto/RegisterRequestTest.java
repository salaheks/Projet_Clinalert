package com.clinalert.doctortracker.dto;

import com.clinalert.doctortracker.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RegisterRequest DTO
 * Target: Cover all getters, setters, constructors
 * Impact: +0.8% coverage
 */
@DisplayName("DTO Tests - RegisterRequest")
class RegisterRequestTest {

    @Test
    @DisplayName("Default constructor should create empty object")
    void defaultConstructor_ShouldWork() {
        RegisterRequest request = new RegisterRequest();
        assertNotNull(request);
    }

    @Test
    @DisplayName("All setters and getters should work")
    void gettersAndSetters_ShouldWork() {
        RegisterRequest request = new RegisterRequest();

        request.setEmail("doctor@test.com");
        request.setPassword("password123");
        request.setRole(User.UserRole.DOCTOR);
        request.setName("Dr. Test");
        request.setSpecialty("Cardiology");
        request.setAge(35);
        request.setGender("M");
        request.setDoctorId("doctor-123");

        assertEquals("doctor@test.com", request.getEmail());
        assertEquals("password123", request.getPassword());
        assertEquals(User.UserRole.DOCTOR, request.getRole());
        assertEquals("Dr. Test", request.getName());
        assertEquals("Cardiology", request.getSpecialty());
        assertEquals(35, request.getAge());
        assertEquals("M", request.getGender());
        assertEquals("doctor-123", request.getDoctorId());
    }

    @Test
    @DisplayName("Patient role with all fields")
    void patientRole_WithAllFields_ShouldWork() {
        RegisterRequest request = new RegisterRequest();

        request.setRole(User.UserRole.PATIENT);
        request.setAge(25);
        request.setGender("F");
        request.setDoctorId("doc-456");

        assertEquals(User.UserRole.PATIENT, request.getRole());
        assertEquals(25, request.getAge());
        assertEquals("F", request.getGender());
        assertEquals("doc-456", request.getDoctorId());
    }
}

package com.clinalert.doctortracker.dto;

import com.clinalert.doctortracker.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Couverture DTOs")
class DtoCoverageTest {

    @Test
    @DisplayName("RegisterRequest - Full Coverage")
    void registerRequest_FullCoverage() {
        // Test Builder
        RegisterRequest req1 = RegisterRequest.builder()
                .email("test@test.com")
                .password("pass")
                .role(User.UserRole.DOCTOR)
                .name("John")
                .specialty("Cardio")
                .phoneNumber("123456")
                .age(30)
                .gender("M")
                .doctorId("doc-1")
                .build();

        // Test All Args Constructor via Builder (Lombok usually uses all-args for
        // builder)
        // Test Getters
        assertThat(req1.getEmail()).isEqualTo("test@test.com");
        assertThat(req1.getPassword()).isEqualTo("pass");
        assertThat(req1.getRole()).isEqualTo(User.UserRole.DOCTOR);
        assertThat(req1.getName()).isEqualTo("John");
        assertThat(req1.getSpecialty()).isEqualTo("Cardio");
        assertThat(req1.getPhoneNumber()).isEqualTo("123456");
        assertThat(req1.getAge()).isEqualTo(30);
        assertThat(req1.getGender()).isEqualTo("M");
        assertThat(req1.getDoctorId()).isEqualTo("doc-1");

        // Test Setters
        RegisterRequest req2 = new RegisterRequest();
        req2.setEmail("test@test.com");
        req2.setPassword("pass");
        req2.setRole(User.UserRole.DOCTOR);
        req2.setName("John");
        req2.setSpecialty("Cardio");
        req2.setPhoneNumber("123456");
        req2.setAge(30);
        req2.setGender("M");
        req2.setDoctorId("doc-1");

        // Test equals and hashCode
        assertThat(req1).isEqualTo(req2);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
        assertThat(req1).isNotEqualTo(new Object());
        assertThat(req1).isNotEqualTo(null);

        // Test toString
        assertThat(req1.toString()).contains("test@test.com", "John");

        // Test canEqual
        assertThat(req1.canEqual(req2)).isTrue();

        // Test Builder toString
        assertThat(RegisterRequest.builder().toString()).isNotNull();
    }

    @Test
    @DisplayName("LoginRequest - Full Coverage")
    void loginRequest_FullCoverage() {
        // Test All Args Constructor
        LoginRequest req1 = new LoginRequest("test@test.com", "pass");

        // Test Getters
        assertThat(req1.getEmail()).isEqualTo("test@test.com");
        assertThat(req1.getPassword()).isEqualTo("pass");

        // Test Setters
        LoginRequest req2 = new LoginRequest();
        req2.setEmail("test@test.com");
        req2.setPassword("pass");

        // Test equals and hashCode
        assertThat(req1).isEqualTo(req2);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());

        // Test toString
        assertThat(req1.toString()).isNotNull();
    }

    @Test
    @DisplayName("RegisterRequest - Equality & HashCode")
    void verifyRegisterRequestEquality() {
        RegisterRequest r1 = RegisterRequest.builder().email("e").build();
        RegisterRequest r2 = RegisterRequest.builder().email("e").build();
        RegisterRequest r3 = RegisterRequest.builder().email("diff").build();

        // Identity
        assertThat(r1).isEqualTo(r1);

        // Equality
        assertThat(r1).isEqualTo(r2);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());

        // Inequality
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1).isNotEqualTo(new Object());
        assertThat(r1).isNotEqualTo(r3);

        // ToString
        assertThat(r1.toString()).isNotNull();
        // CanEqual
        assertThat(r1.canEqual(r2)).isTrue();
    }

    @Test
    @DisplayName("LoginRequest - Equality & HashCode")
    void verifyLoginRequestEquality() {
        LoginRequest r1 = new LoginRequest("e", "p");
        LoginRequest r2 = new LoginRequest("e", "p");
        LoginRequest r3 = new LoginRequest("diff", "p");

        // Identity
        assertThat(r1).isEqualTo(r1);

        // Equality
        assertThat(r1).isEqualTo(r2);
        assertThat(r2).isEqualTo(r1);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());

        // Inequality
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1).isNotEqualTo(new Object());
        assertThat(r1).isNotEqualTo(r3);

        // ToString
        assertThat(r1.toString()).isNotNull();
    }

    @Test
    @DisplayName("LoginResponse - Full Coverage")
    void loginResponse_FullCoverage() {
        // Test All Args Constructor (5 args): token, type, userId, email, role
        LoginResponse resp1 = new LoginResponse("token", "Bearer", "user-1", "test@test.com", "DOCTOR");

        // Test Getters
        assertThat(resp1.getToken()).isEqualTo("token");
        assertThat(resp1.getType()).isEqualTo("Bearer");
        assertThat(resp1.getUserId()).isEqualTo("user-1");
        assertThat(resp1.getEmail()).isEqualTo("test@test.com");
        assertThat(resp1.getRole()).isEqualTo("DOCTOR");

        // Test Constructor with 4 args
        LoginResponse resp2 = new LoginResponse("token", "user-1", "test@test.com", "DOCTOR");
        // Verify default type
        assertThat(resp2.getType()).isEqualTo("Bearer");

        // Test Setters
        LoginResponse resp3 = new LoginResponse();
        resp3.setToken("token");
        resp3.setUserId("user-1");
        resp3.setEmail("test@test.com");
        resp3.setRole("DOCTOR");
        resp3.setType("Bearer");

        // Verify 5 args constructor behavior
    }

    @Test
    @DisplayName("Equality & HashCode Test")
    void verifyEquality() {
        LoginResponse r1 = new LoginResponse("t", "u", "e", "r");
        LoginResponse r2 = new LoginResponse("t", "u", "e", "r");
        LoginResponse r3 = new LoginResponse("diff", "u", "e", "r");

        // Identity
        assertThat(r1).isEqualTo(r1);

        // Equality
        assertThat(r1).isEqualTo(r2);
        assertThat(r2).isEqualTo(r1);
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());

        // Inequality
        assertThat(r1).isNotEqualTo(null);
        assertThat(r1).isNotEqualTo(new Object());
        assertThat(r1).isNotEqualTo(r3);

        // ToString
        assertThat(r1.toString()).isNotNull();
    }
}

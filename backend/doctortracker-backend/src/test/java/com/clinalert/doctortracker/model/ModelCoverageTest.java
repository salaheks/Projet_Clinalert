package com.clinalert.doctortracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Couverture Models")
class ModelCoverageTest {

    @Test
    @DisplayName("User - Full Coverage (Lombok)")
    void user_FullCoverage() {
        LocalDateTime now = LocalDateTime.now();

        // Test All Args Constructor via Builder or directly if visible
        User user1 = User.builder()
                .id("1")
                .email("test@test.com")
                .password("pass")
                .role(User.UserRole.DOCTOR)
                .enabled(true)
                .firstName("John")
                .lastName("Doe")
                .phone("123")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Test Setters
        User user2 = new User();
        user2.setId("1");
        user2.setEmail("test@test.com");
        user2.setPassword("pass");
        user2.setRole(User.UserRole.DOCTOR);
        user2.setEnabled(true);
        user2.setFirstName("John");
        user2.setLastName("Doe");
        user2.setPhone("123");
        user2.setCreatedAt(now);
        user2.setUpdatedAt(now);

        // Test Getters
        assertThat(user1.getId()).isEqualTo("1");
        assertThat(user1.getCreatedAt()).isEqualTo(now);
        assertThat(user1.getUpdatedAt()).isEqualTo(now);

        // Test equals and hashCode
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1).isNotEqualTo(new Object());
        assertThat(user1).isNotEqualTo(null);

        // Test toString
        assertThat(user1.toString()).contains("test@test.com");

        // Test canEqual
        assertThat(user1.canEqual(user2)).isTrue();

        // Test Lifecycle hooks
        User user3 = new User();
        user3.onCreate();
        assertThat(user3.getCreatedAt()).isNotNull();
        assertThat(user3.getEnabled()).isTrue(); // default

        user3.onUpdate();
        assertThat(user3.getUpdatedAt()).isNotNull();

        // Test Authority
        user3.setRole(User.UserRole.PATIENT); // Role needed for getAuthorities
        assertThat(user3.getAuthorities()).hasSize(1);
    }

    @Test
    @DisplayName("Verify Equality Branches")
    void verifyEquality() {
        User u1 = new User();
        u1.setId("1");
        User u2 = new User();
        u2.setId("1");
        User u3 = new User();
        u3.setId("2");

        assertThat(u1.canEqual(u2)).isTrue();
        assertThat(u1).isEqualTo(u1); // self
        assertThat(u1).isEqualTo(u2); // same id
        assertThat(u1).isNotEqualTo(null); // null
        assertThat(u1).isNotEqualTo(new Object()); // different class
        assertThat(u1).isNotEqualTo(u3); // different id

        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
        assertThat(u1.toString()).isNotNull();
    }
}

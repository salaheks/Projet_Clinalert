package com.clinalert.doctortracker.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests Modèle User")
class UserTest {

    @Test
    @DisplayName("getUsername - Doit retourner l'email")
    void getUsername_ShouldReturnEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        assertThat(user.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("User Builder - Doit créer un objet valide")
    void builder_ShouldCreateValidObject() {
        User user = User.builder()
                .id("1")
                .email("test@example.com")
                .password("pass")
                .role(User.UserRole.DOCTOR)
                .firstName("John")
                .lastName("Doe")
                .build();

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo("1");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(User.UserRole.DOCTOR);
    }

    @Test
    @DisplayName("Getters/Setters - Doit fonctionner correctement")
    void settersAndGetters_ShouldWork() {
        User user = new User();
        user.setId("2");
        user.setPhone("1234567890");
        user.setEnabled(true);

        assertThat(user.getId()).isEqualTo("2");
        assertThat(user.getPhone()).isEqualTo("1234567890");
        assertThat(user.isEnabled()).isTrue();
    }
}

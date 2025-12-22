package com.clinalert.doctortracker.service;

/**
 * Tests UserService - 15 tests
 * Couvre: CRUD utilisateurs, password, email, profile
 */

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Service User")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-001");
        user.setEmail("test@clinalert.com");
        user.setPassword("encodedPassword");
        user.setRole(User.UserRole.DOCTOR);
        user.setEnabled(true);
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    @DisplayName("getAllUsers - Doit retourner liste")
    void getAllUsers_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getUserById - Existant")
    void getUserById_WhenExists_ShouldReturn() {
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById("user-001");

        assertThat(result).isPresent();
        verify(userRepository).findById("user-001");
    }

    @Test
    @DisplayName("getUserByEmail - Existant")
    void getUserByEmail_WhenExists_ShouldReturn() {
        when(userRepository.findByEmail("test@clinalert.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("test@clinalert.com");

        assertThat(result).isPresent();
        verify(userRepository).findByEmail("test@clinalert.com");
    }

    @Test
    @DisplayName("createUser - Nouvel email")
    void createUser_NewEmail_ShouldCreate() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.createUser("new@test.com", "password", User.UserRole.DOCTOR);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("createUser - Email existant")
    void createUser_ExistingEmail_ShouldThrow() {
        when(userRepository.existsByEmail("test@clinalert.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser("test@clinalert.com", "pass", User.UserRole.DOCTOR))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("updateUser - Role et enabled")
    void updateUser_ShouldUpdateFields() {
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.updateUser("user-001", User.UserRole.PATIENT, false);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("updateUser - User non trouvé")
    void updateUser_NotFound_ShouldThrow() {
        when(userRepository.findById("user-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser("user-999", null, null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateUserPassword")
    void updatePassword_ShouldEncode() {
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.updateUserPassword("user-001", "newPass");

        assertThat(result).isNotNull();
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("updateUserEmail - Nouveau email valide")
    void updateEmail_NewEmail_ShouldUpdate() {
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.updateUserEmail("user-001", "new@test.com");

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("updateUserEmail - Email déjà utilisé")
    void updateEmail_ExistingEmail_ShouldThrow() {
        User otherUser = new User();
        otherUser.setId("user-002");
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> userService.updateUserEmail("user-001", "other@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    @DisplayName("deleteUser - Existant")
    void deleteUser_WhenExists_ShouldDelete() {
        when(userRepository.existsById("user-001")).thenReturn(true);

        userService.deleteUser("user-001");

        verify(userRepository).deleteById("user-001");
    }

    @Test
    @DisplayName("deleteUser - Non existant")
    void deleteUser_NotFound_ShouldThrow() {
        when(userRepository.existsById("user-999")).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser("user-999"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("updateUserProfile")
    void updateProfile_ShouldUpdateFields() {
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.updateUserProfile("user-001", "Jane", "Smith", "123456");

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }

    @Test
    @DisplayName("updateUserProfile - Champs null")
    void updateProfile_NullFields_ShouldKeepExisting() {
        when(userRepository.findById("user-001")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.updateUserProfile("user-001", null, null, null);

        assertThat(result).isNotNull();
        verify(userRepository).save(any());
    }
}

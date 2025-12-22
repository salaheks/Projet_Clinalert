package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Direct unit tests for AuthDataSeeder using reflection
 * No Spring context needed - manual instantiation
 * Target: 77% → 100% (+23% package impact)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthDataSeeder Unit Tests")
class AuthDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthDataSeeder seeder;

    @BeforeEach
    void setUp() throws Exception {
        seeder = new AuthDataSeeder();

        // Inject mocks via reflection
        Field userRepoField = AuthDataSeeder.class.getDeclaredField("userRepository");
        userRepoField.setAccessible(true);
        userRepoField.set(seeder, userRepository);

        Field encoderField = AuthDataSeeder.class.getDeclaredField("passwordEncoder");
        encoderField.setAccessible(true);
        encoderField.set(seeder, passwordEncoder);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }

    @Test
    @DisplayName("run - Should create all default users")
    void run_ShouldCreateAllDefaultUsers() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        seeder.run();

        // Should create: 1 admin + 2 doctors + 2 patients = 5 users
        verify(userRepository, times(5)).findByEmail(anyString());
        verify(userRepository, times(5)).save(any(User.class));
        verify(passwordEncoder, times(5)).encode(anyString());
    }

    @Test
    @DisplayName("run - Users exist with FORCE_PASSWORD_RESET true - Should update passwords")
    void run_UsersExist_ForceResetTrue_ShouldUpdatePasswords() throws Exception {
        User existingUser = new User();
        existingUser.setEmail("admin@clinalert.com");
        existingUser.setPassword("old_password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        seeder.run();

        // Should update all 5 users (FORCE_PASSWORD_RESET = true)
        verify(userRepository, times(5)).save(any(User.class));
    }

    @Test
    @DisplayName("createOrUpdateUser - User not exists - Should create new user")
    void createOrUpdateUser_UserNotExists_ShouldCreate() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Call private method via reflection
        Method method = AuthDataSeeder.class.getDeclaredMethod(
                "createOrUpdateUser", String.class, String.class, User.UserRole.class);
        method.setAccessible(true);
        method.invoke(seeder, "test@example.com", "password123", User.UserRole.DOCTOR);

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("createOrUpdateUser - User exists - FORCE_RESET true - Should update")
    void createOrUpdateUser_UserExists_ForceReset_ShouldUpdate() throws Exception {
        User existing = new User();
        existing.setEmail("existing@example.com");
        existing.setPassword("old_hash");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Method method = AuthDataSeeder.class.getDeclaredMethod(
                "createOrUpdateUser", String.class, String.class, User.UserRole.class);
        method.setAccessible(true);
        method.invoke(seeder, "existing@example.com", "newpass", User.UserRole.PATIENT);

        verify(userRepository).save(existing);
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    @DisplayName("createOrUpdateUser - Test all roles - ADMIN, DOCTOR, PATIENT")
    void createOrUpdateUser_AllRoles_ShouldWork() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Method method = AuthDataSeeder.class.getDeclaredMethod(
                "createOrUpdateUser", String.class, String.class, User.UserRole.class);
        method.setAccessible(true);

        method.invoke(seeder, "admin@test.com", "pass", User.UserRole.ADMIN);
        method.invoke(seeder, "doctor@test.com", "pass", User.UserRole.DOCTOR);
        method.invoke(seeder, "patient@test.com", "pass", User.UserRole.PATIENT);

        verify(userRepository, times(3)).save(any(User.class));
    }
}

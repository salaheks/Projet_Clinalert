package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthDataSeeder Coverage Tests")
class AuthDataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthDataSeeder authDataSeeder;

    @Test
    @DisplayName("Run - Should Create New Users If Not Exist")
    void run_ShouldCreateUsers_IfNotExist() throws Exception {
        // Mock repository to return empty for all checks
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");

        authDataSeeder.run();

        // Verify save is called for admin, doctors (2), patients (2) = 5 times
        verify(userRepository, times(5)).save(any(User.class));
    }

    @Test
    @DisplayName("Run - Should Update Password If Exists")
    void run_ShouldUpdateUser_IfExists() throws Exception {
        // Mock repository to return existing user
        User existingUser = new User();
        existingUser.setEmail("admin@clinalert.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPass");

        authDataSeeder.run();

        // Verify save is called 5 times (updates)
        verify(userRepository, times(5)).save(any(User.class));
        verify(passwordEncoder, atLeastOnce()).encode(anyString());
    }
}

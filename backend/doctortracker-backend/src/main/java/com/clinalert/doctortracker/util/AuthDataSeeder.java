package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    // Set to true to force password reset for all demo users
    private static final boolean FORCE_PASSWORD_RESET = true;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking authentication seed data...");

        // Create or update Admin User
        createOrUpdateUser("admin@clinalert.com", "admin123", User.UserRole.ADMIN);

        // Create or update Doctor Users
        createOrUpdateUser("house@clinalert.com", "doctor123", User.UserRole.DOCTOR);
        createOrUpdateUser("cameron@clinalert.com", "doctor123", User.UserRole.DOCTOR);

        // Create or update Patient Users
        createOrUpdateUser("john.doe@clinalert.com", "patient123", User.UserRole.PATIENT);
        createOrUpdateUser("luc.moreau@clinalert.com", "patient123", User.UserRole.PATIENT);

        log.info("Auth seed data check completed!");
    }

    private void createOrUpdateUser(String email, String password, User.UserRole role) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            if (FORCE_PASSWORD_RESET) {
                User user = existingUser.get();
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                log.info("Updated password for user: {} / [PROTECTED]", email);
            } else {
                log.info("User already exists: {}", email);
            }
        } else {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);
            userRepository.save(user);
            log.info("Created user: {} / [PROTECTED]", email);
        }
    }
}

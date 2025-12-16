package com.clinalert.doctortracker.util;

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthDataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Set to true to force password reset for all demo users
    private static final boolean FORCE_PASSWORD_RESET = true;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking authentication seed data...");

        // Create or update Admin User
        createOrUpdateUser("admin@clinalert.com", "admin123", User.UserRole.ADMIN);

        // Create or update Doctor Users
        createOrUpdateUser("house@clinalert.com", "doctor123", User.UserRole.DOCTOR);
        createOrUpdateUser("cameron@clinalert.com", "doctor123", User.UserRole.DOCTOR);

        // Create or update Patient Users
        createOrUpdateUser("john.doe@clinalert.com", "patient123", User.UserRole.PATIENT);
        createOrUpdateUser("luc.moreau@clinalert.com", "patient123", User.UserRole.PATIENT);

        System.out.println("Auth seed data check completed!");
    }

    private void createOrUpdateUser(String email, String password, User.UserRole role) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            if (FORCE_PASSWORD_RESET) {
                User user = existingUser.get();
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                System.out.println("Updated password for user: " + email + " / " + password);
            } else {
                System.out.println("User already exists: " + email);
            }
        } else {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setEnabled(true);
            userRepository.save(user);
            System.out.println("Created user: " + email + " / " + password);
        }
    }
}

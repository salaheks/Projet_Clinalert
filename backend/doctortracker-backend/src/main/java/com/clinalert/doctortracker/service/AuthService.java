package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.dto.LoginRequest;
import com.clinalert.doctortracker.dto.LoginResponse;
import com.clinalert.doctortracker.dto.RegisterRequest;
import com.clinalert.doctortracker.model.Doctor;
import com.clinalert.doctortracker.model.Patient;
import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.DoctorRepository;
import com.clinalert.doctortracker.repository.PatientRepository;
import com.clinalert.doctortracker.repository.UserRepository;
import com.clinalert.doctortracker.security.JwtTokenProvider;
import com.clinalert.doctortracker.util.AppConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;

    private final DoctorRepository doctorRepository;

    private final PatientRepository patientRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider;

    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        String safeEmail = sanitizeForLog(request.getEmail());
        log.info("=== LOGIN ATTEMPT ===");
        log.info("Email: {}", safeEmail);
        log.debug("Password received: [PROTECTED]");

        // Check if user exists first
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser == null) {
            log.warn("ERROR: User not found in database for email: {}", safeEmail);
        } else {
            log.info("User found! ID: {}", existingUser.getId());
            boolean matches = passwordEncoder.matches(request.getPassword(), existingUser.getPassword());
            log.info("Password matches: {}", matches);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException(
                            AppConstants.ERROR_USER_NOT_FOUND_PREFIX + request.getEmail()));

            String token = tokenProvider.generateToken(user, user.getId(), user.getRole().name());
            log.info("Login successful! Token generated.");

            return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name());
        } catch (Exception e) {
            log.error("Authentication FAILED: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String safeEmail = sanitizeForLog(request.getEmail());
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        user = userRepository.save(user);
        log.info("Registered new user with email: {}", safeEmail);

        // Create associated Doctor or Patient profile
        if (request.getRole() == User.UserRole.DOCTOR && request.getName() != null) {
            Doctor doctor = new Doctor();
            doctor.setName(request.getName());
            doctor.setSpecialty(request.getSpecialty() != null ? request.getSpecialty() : "General");
            doctor.setEmail(request.getEmail());
            doctor.setPhoneNumber(request.getPhoneNumber());
            doctorRepository.save(doctor);
            log.info("Created Doctor profile for user: {}", safeEmail);
        } else if (request.getRole() == User.UserRole.PATIENT && request.getName() != null) {
            Patient patient = new Patient();
            patient.setName(request.getName());
            patient.setAge(request.getAge() != null ? request.getAge() : 0);
            patient.setGender(request.getGender() != null ? request.getGender() : "Unknown");
            patient.setDoctorId(request.getDoctorId());
            patient.setStatus("active");
            patientRepository.save(patient);
        }

        // Generate token
        String token = tokenProvider.generateToken(user, user.getId(), user.getRole().name());

        return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private String sanitizeForLog(String input) {
        if (input == null) {
            return "null";
        }
        return input.replaceAll("[\r\n]", "_");
    }
}

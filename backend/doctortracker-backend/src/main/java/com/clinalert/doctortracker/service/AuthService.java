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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + request.getEmail());
        System.out.println("Password received: " + request.getPassword());

        // Check if user exists first
        User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser == null) {
            System.out.println("ERROR: User not found in database!");
        } else {
            System.out.println("User found! ID: " + existingUser.getId());
            System.out.println("Stored password hash: " + existingUser.getPassword());
            boolean matches = passwordEncoder.matches(request.getPassword(), existingUser.getPassword());
            System.out.println("Password matches: " + matches);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = tokenProvider.generateToken(user, user.getId(), user.getRole().name());
            System.out.println("Login successful! Token generated.");

            return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name());
        } catch (Exception e) {
            System.out.println("Authentication FAILED: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        user = userRepository.save(user);

        // Create associated Doctor or Patient profile
        if (request.getRole() == User.UserRole.DOCTOR && request.getName() != null) {
            Doctor doctor = new Doctor();
            doctor.setName(request.getName());
            doctor.setSpecialty(request.getSpecialty() != null ? request.getSpecialty() : "General");
            doctor.setEmail(request.getEmail());
            doctor.setPhoneNumber(request.getPhoneNumber());
            doctorRepository.save(doctor);
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
}

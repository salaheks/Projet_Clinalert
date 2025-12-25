package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.dto.LoginRequest;
import com.clinalert.doctortracker.dto.LoginResponse;
import com.clinalert.doctortracker.dto.RegisterRequest;
import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request) {
        log.info("=== CONTROLLER: LOGIN ENDPOINT HIT ===");
        log.info("Request email: {}", request.getEmail());
        try {
            LoginResponse response = authService.login(request);
            log.info("=== LOGIN SUCCESS ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("=== LOGIN ERROR: {} ===", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put(com.clinalert.doctortracker.util.AppConstants.KEY_ERROR, "Invalid email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping(value = "/register", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put(com.clinalert.doctortracker.util.AppConstants.KEY_ERROR, e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body("Not authenticated");
            }

            Map<String, Object> response = new HashMap<>();
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_ID, user.getId());
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_EMAIL, user.getEmail());
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_ROLE, user.getRole().name());
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_ENABLED, user.isEnabled());
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_FIRST_NAME, user.getFirstName());
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_LAST_NAME, user.getLastName());
            response.put(com.clinalert.doctortracker.util.AppConstants.KEY_PHONE, user.getPhone());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

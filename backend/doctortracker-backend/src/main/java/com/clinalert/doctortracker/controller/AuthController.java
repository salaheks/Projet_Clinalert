package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.dto.LoginRequest;
import com.clinalert.doctortracker.dto.LoginResponse;
import com.clinalert.doctortracker.dto.RegisterRequest;
import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("=== CONTROLLER: LOGIN ENDPOINT HIT ===");
        System.out.println("Request email: " + request.getEmail());
        try {
            LoginResponse response = authService.login(request);
            System.out.println("=== LOGIN SUCCESS ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== LOGIN ERROR: " + e.getMessage() + " ===");
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping(value = "/register", produces = "application/json", consumes = "application/json")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            if (user == null) {
                return ResponseEntity.status(401).body("Not authenticated");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().name());
            response.put("enabled", user.isEnabled());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("phone", user.getPhone());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

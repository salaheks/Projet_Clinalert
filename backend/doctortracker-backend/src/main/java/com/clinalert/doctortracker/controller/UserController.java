package com.clinalert.doctortracker.controller;

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<Map<String, Object>> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::userToMap)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable @NonNull String id) {
        return userService.getUserById(Objects.requireNonNull(id))
                .map(user -> ResponseEntity.ok(userToMap(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get(com.clinalert.doctortracker.util.AppConstants.KEY_EMAIL);
            String password = request.get("password");
            String roleStr = request.get(com.clinalert.doctortracker.util.AppConstants.KEY_ROLE);

            if (email == null || password == null || roleStr == null) {
                return ResponseEntity.badRequest().body("Email, password and role are required");
            }

            User.UserRole role = User.UserRole.valueOf(roleStr.toUpperCase());
            User user = userService.createUser(email, password, role);
            return ResponseEntity.ok(userToMap(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable @NonNull String id,
            @RequestBody Map<String, Object> request) {
        try {
            User.UserRole role = null;
            Boolean enabled = null;

            if (request.containsKey(com.clinalert.doctortracker.util.AppConstants.KEY_ROLE)) {
                role = User.UserRole.valueOf(
                        ((String) request.get(com.clinalert.doctortracker.util.AppConstants.KEY_ROLE)).toUpperCase());
            }
            if (request.containsKey(com.clinalert.doctortracker.util.AppConstants.KEY_ENABLED)) {
                enabled = (Boolean) request.get(com.clinalert.doctortracker.util.AppConstants.KEY_ENABLED);
            }

            User user = userService.updateUser(Objects.requireNonNull(id), role, enabled);
            return ResponseEntity.ok(userToMap(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Object> updatePassword(@PathVariable @NonNull String id,
            @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("password");
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            User user = userService.updateUserPassword(Objects.requireNonNull(id), newPassword);
            return ResponseEntity.ok(userToMap(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<Object> updateEmail(@PathVariable @NonNull String id,
            @RequestBody Map<String, String> request) {
        try {
            String newEmail = request.get(com.clinalert.doctortracker.util.AppConstants.KEY_EMAIL);
            if (newEmail == null || newEmail.isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            User user = userService.updateUserEmail(Objects.requireNonNull(id), newEmail);
            return ResponseEntity.ok(userToMap(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable @NonNull String id) {
        try {
            userService.deleteUser(Objects.requireNonNull(id));
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Object> updateProfile(@PathVariable @NonNull String id,
            @RequestBody Map<String, String> request) {
        try {
            String firstName = request.get(com.clinalert.doctortracker.util.AppConstants.KEY_FIRST_NAME);
            String lastName = request.get(com.clinalert.doctortracker.util.AppConstants.KEY_LAST_NAME);
            String phone = request.get(com.clinalert.doctortracker.util.AppConstants.KEY_PHONE);

            User user = userService.updateUserProfile(Objects.requireNonNull(id), firstName, lastName, phone);
            return ResponseEntity.ok(userToMap(user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Convert User to Map (hide password)
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_ID, user.getId());
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_EMAIL, user.getEmail());
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_ROLE, user.getRole().name());
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_ENABLED, user.isEnabled());
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_FIRST_NAME, user.getFirstName());
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_LAST_NAME, user.getLastName());
        map.put(com.clinalert.doctortracker.util.AppConstants.KEY_PHONE, user.getPhone());
        map.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return map;
    }
}

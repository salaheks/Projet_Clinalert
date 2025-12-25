package com.clinalert.doctortracker.service;

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.repository.UserRepository;
import com.clinalert.doctortracker.util.AppConstants;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(@NonNull String id) {
        return userRepository.findById(Objects.requireNonNull(id));
    }

    public Optional<User> getUserByEmail(@NonNull String email) {
        return userRepository.findByEmail(Objects.requireNonNull(email));
    }

    public User createUser(@NonNull String email, @NonNull String password, @NonNull User.UserRole role) {
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        Objects.requireNonNull(role);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @SuppressWarnings("null")
    public User updateUser(@NonNull String id, User.UserRole role, Boolean enabled) {
        Objects.requireNonNull(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        AppConstants.ERROR_USER_NOT_FOUND_PREFIX + id));

        if (role != null) {
            user.setRole(role);
        }
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        return userRepository.save(user);
    }

    public User updateUserPassword(@NonNull String id, @NonNull String newPassword) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(newPassword);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        AppConstants.ERROR_USER_NOT_FOUND_PREFIX + id));

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public User updateUserEmail(@NonNull String id, @NonNull String newEmail) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(newEmail);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        AppConstants.ERROR_USER_NOT_FOUND_PREFIX + id));

        // Check if new email is already taken by another user
        Optional<User> existingUser = userRepository.findByEmail(newEmail);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            throw new IllegalArgumentException("Email already exists: " + newEmail);
        }

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    public void deleteUser(@NonNull String id) {
        Objects.requireNonNull(id);
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException(AppConstants.ERROR_USER_NOT_FOUND_PREFIX + id);
        }
        userRepository.deleteById(id);
    }

    @SuppressWarnings("null")
    public User updateUserProfile(@NonNull String id, String firstName, String lastName, String phone) {
        Objects.requireNonNull(id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        AppConstants.ERROR_USER_NOT_FOUND_PREFIX + id));

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        return userRepository.save(user);
    }
}

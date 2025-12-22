package com.clinalert.doctortracker.controller;

/**
 * Tests UserController - Integration - 12 tests
 * Couvre: CRUD users, password, email, profile
 */

import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests Controller User - Integration")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-001");
        user.setEmail("test@clinalert.com");
        user.setRole(User.UserRole.DOCTOR);
        user.setEnabled(true);
        user.setFirstName("John");
        user.setLastName("Doe");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/users - Retourne tous les users")
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@clinalert.com"));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/users/{id} - Retourne user")
    void getUserById_WhenExists_ShouldReturn() throws Exception {
        when(userService.getUserById("user-001")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@clinalert.com"));

        verify(userService).getUserById("user-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("GET /api/users/{id} - 404 si non trouvé")
    void getUserById_NotFound_ShouldReturn404() throws Exception {
        when(userService.getUserById("user-999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/user-999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/users - Créer user")
    void createUser_ValidData_ShouldCreate() throws Exception {
        when(userService.createUser(any(), any(), any())).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@test.com\",\"password\":\"pass123\",\"role\":\"DOCTOR\"}"))
                .andExpect(status().isOk());

        verify(userService).createUser(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/users/{id} - Update user")
    void updateUser_ShouldUpdate() throws Exception {
        when(userService.updateUser(eq("user-001"), any(), any())).thenReturn(user);

        mockMvc.perform(put("/api/users/user-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"PATIENT\",\"enabled\":false}"))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq("user-001"), any(), any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/users/{id}/password - Update password")
    void updatePassword_ShouldUpdate() throws Exception {
        when(userService.updateUserPassword("user-001", "newPass")).thenReturn(user);

        mockMvc.perform(put("/api/users/user-001/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"newPass\"}"))
                .andExpect(status().isOk());

        verify(userService).updateUserPassword("user-001", "newPass");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/users/{id}/email - Update email")
    void updateEmail_ShouldUpdate() throws Exception {
        when(userService.updateUserEmail("user-001", "new@test.com")).thenReturn(user);

        mockMvc.perform(put("/api/users/user-001/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@test.com\"}"))
                .andExpect(status().isOk());

        verify(userService).updateUserEmail("user-001", "new@test.com");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("DELETE /api/users/{id} - Delete user")
    void deleteUser_ShouldDelete() throws Exception {
        doNothing().when(userService).deleteUser("user-001");

        mockMvc.perform(delete("/api/users/user-001"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("user-001");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/users/{id}/profile - Update profile")
    void updateProfile_ShouldUpdate() throws Exception {
        when(userService.updateUserProfile(eq("user-001"), any(), any(), any())).thenReturn(user);

        mockMvc.perform(put("/api/users/user-001/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"phone\":\"123456\"}"))
                .andExpect(status().isOk());

        verify(userService).updateUserProfile(eq("user-001"), any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("POST /api/users - Invalid data")
    void createUser_InvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\"}")) // Missing password & role
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/users/{id}/password - Empty password")
    void updatePassword_EmptyPassword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/users/user-001/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("PUT /api/users/{id}/email - Empty email")
    void updateEmail_EmptyEmail_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/users/user-001/email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}

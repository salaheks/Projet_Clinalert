package com.clinalert.doctortracker.controller;

/**
 * Tests AuthController - Integration - 6 tests
 * Couvre: login, register, getCurrentUser
 */

import com.clinalert.doctortracker.dto.LoginRequest;
import com.clinalert.doctortracker.dto.LoginResponse;
import com.clinalert.doctortracker.dto.RegisterRequest;
import com.clinalert.doctortracker.model.User;
import com.clinalert.doctortracker.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Tests Controller Auth - Integration")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/login - Success")
    void login_ValidCredentials_ShouldReturnToken() throws Exception {
        LoginResponse response = new LoginResponse("jwt-token", "user-001", "test@test.com", "DOCTOR");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid credentials")
    void login_InvalidCredentials_ShouldReturnBadRequest() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Invalid"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register - Success")
    void register_ValidData_ShouldReturnToken() throws Exception {
        LoginResponse response = new LoginResponse("jwt-token", "user-002", "new@test.com", "DOCTOR");
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@test.com\",\"password\":\"pass123\",\"role\":\"DOCTOR\"}"))
                .andExpect(status().isOk());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/auth/register - Error")
    void register_Error_ShouldReturnBadRequest() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new RuntimeException("Email exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"exist@test.com\",\"password\":\"pass\",\"role\":\"DOCTOR\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/auth/me - Authenticated")
    void getCurrentUser_Authenticated_ShouldReturnUser() throws Exception {
        User user = new User();
        user.setId("user-001");
        user.setEmail("test@test.com");
        user.setRole(User.UserRole.DOCTOR);

        when(authService.getCurrentUser()).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Not authenticated")
    void getCurrentUser_NotAuthenticated_ShouldReturn401() throws Exception {
        when(authService.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

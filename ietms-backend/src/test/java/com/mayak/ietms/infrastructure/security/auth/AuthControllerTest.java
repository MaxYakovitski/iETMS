package com.mayak.ietms.infrastructure.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.infrastructure.config.SecurityConfig;
import com.mayak.ietms.infrastructure.notify.SlackAlertService;
import com.mayak.ietms.infrastructure.security.RestAuthenticationEntryPoint;
import com.mayak.ietms.infrastructure.security.auth.dto.LoginRequest;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("AuthController")
public class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean AuthService authService;
    @MockitoBean SlackAlertService slackAlertService;

    @Test
    @DisplayName("POST /api/auth/login — valid credentials return token")
    public void login_validCredentials_returnsToken() throws Exception {
        when(authService.login("user@test.com", "secret")).thenReturn("jwt-token");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("user@test.com", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login — missing body returns 400")
    public void login_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
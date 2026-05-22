package com.mayak.ietms.infrastructure.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.ietms.infrastructure.config.SecurityConfig;
import com.mayak.ietms.infrastructure.notify.SlackAlertService;
import com.mayak.ietms.infrastructure.security.RestAuthenticationEntryPoint;
import com.mayak.ietms.infrastructure.security.auth.dto.LoginRequest;
import com.mayak.ietms.infrastructure.security.auth.dto.LoginResponse;
import com.mayak.ietms.infrastructure.security.auth.dto.RefreshTokenRequest;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import com.mayak.ietms.infrastructure.web.exception.ApiExceptionHandler;
import com.mayak.ietms.shared.exception.business.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class, ApiExceptionHandler.class})
@DisplayName("AuthController")
public class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean JwtService jwtService;
    @MockitoBean AuthService authService;
    @MockitoBean SlackAlertService slackAlertService;

    @Test
    @DisplayName("POST /api/auth/login — valid credentials return token pair")
    void login_validCredentials_returnsTokenPair() throws Exception {
        when(authService.login("user@test.com", "secret"))
                .thenReturn(new LoginResponse("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@test.com", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login — invalid credentials return 401")
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login("user@test.com", "wrong"))
                .thenThrow(new AuthenticationException("Invalid email or password!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("user@test.com", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
    }

    @Test
    @DisplayName("POST /api/auth/login — missing body returns 400")
    void login_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/refresh — valid token returns new token pair")
    void refresh_validToken_returnsNewTokenPair() throws Exception {
        when(authService.refresh("raw-refresh"))
                .thenReturn(new LoginResponse("new-access", "new-refresh"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("raw-refresh"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh — expired or revoked token returns 401")
    void refresh_invalidToken_returns401() throws Exception {
        when(authService.refresh("bad-token"))
                .thenThrow(new AuthenticationException("Refresh token expired or revoked"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("bad-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh — missing body returns 400")
    void refresh_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/logout — valid token returns 204")
    void logout_validToken_returns204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest("raw-refresh"))))
                .andExpect(status().isNoContent());
        verify(authService).logout("raw-refresh");
    }

    @Test
    @DisplayName("POST /api/auth/logout — missing body returns 400")
    void logout_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
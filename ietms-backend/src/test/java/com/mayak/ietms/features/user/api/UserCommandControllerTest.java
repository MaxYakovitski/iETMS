package com.mayak.ietms.features.user.api;

import com.mayak.ietms.features.user.application.UserCommandService;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.infrastructure.config.SecurityConfig;
import com.mayak.ietms.infrastructure.notify.SlackAlertService;
import com.mayak.ietms.infrastructure.security.RestAuthenticationEntryPoint;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserCommandController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("UserCommandController")
public class UserCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean
    JwtService jwtService;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    SlackAlertService slackAlertService;

    @MockitoBean
    UserCommandService userCommandService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    static RequestPostProcessor asManager() {
        return authentication(new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("MANAGE_USERS"))));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users — no auth → 401")
    public void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/users/{id} — no auth → 401")
    public void update_noAuth_returns401() throws Exception {
        mockMvc.perform(put("/api/users/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — no auth → 401")
    public void delete_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/users/1")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no MANAGE_USERS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users — no MANAGE_USERS → 403")
    public void create_noAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/users").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/{id} — no MANAGE_USERS → 403")
    public void update_noAuthority_returns403() throws Exception {
        mockMvc.perform(put("/api/users/1").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — no MANAGE_USERS → 403")
    public void delete_noAuthority_returns403() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(asUser()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/password — no MANAGE_USERS → 403")
    public void changePassword_noAuthority_returns403() throws Exception {
        mockMvc.perform(patch("/api/users/1/password").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/status — no MANAGE_USERS → 403")
    public void changeStatus_noAuthority_returns403() throws Exception {
        mockMvc.perform(patch("/api/users/1/status").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — MANAGE_USERS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/users — MANAGE_USERS → 200")
    public void create_withAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/users").with(asManager())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/users/{id} — MANAGE_USERS → 200")
    public void update_withAuthority_returns200() throws Exception {
        mockMvc.perform(put("/api/users/1").with(asManager())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} — MANAGE_USERS → 200")
    public void delete_withAuthority_returns200() throws Exception {
        mockMvc.perform(delete("/api/users/1").with(asManager()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/password — MANAGE_USERS → 200")
    public void changePassword_withAuthority_returns200() throws Exception {
        mockMvc.perform(patch("/api/users/1/password").with(asManager())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/status — MANAGE_USERS → 200")
    public void changeStatus_withAuthority_returns200() throws Exception {
        mockMvc.perform(patch("/api/users/1/status").with(asManager())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }
}
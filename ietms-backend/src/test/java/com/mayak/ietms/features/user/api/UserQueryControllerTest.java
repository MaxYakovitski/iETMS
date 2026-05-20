package com.mayak.ietms.features.user.api;

import com.mayak.ietms.features.user.application.UserProfileQueryService;
import com.mayak.ietms.features.user.application.UserQueryService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserQueryController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("UserQueryController")
public class UserQueryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean SlackAlertService slackAlertService;

    @MockitoBean UserQueryService userQueryService;
    @MockitoBean UserProfileQueryService userProfileQueryService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/users — no auth → 401")
    public void findAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("GET /api/users/{id} — no auth → 401")
    public void findById_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users/1")).andExpect(status().isUnauthorized());
    }

    @Test @DisplayName("GET /api/users/me — no auth → 401")
    public void getMe_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test @DisplayName("GET /api/users — authenticated → 200")
    public void findAll_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/users").with(asUser())).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/users/{id} — authenticated → 200")
    public void findById_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/users/1").with(asUser())).andExpect(status().isOk());
    }

    @Test @DisplayName("GET /api/users/me — authenticated → 200")
    public void getMe_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/users/me").with(asUser())).andExpect(status().isOk());
    }

}
package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseQueryService;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LicenseQueryController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("LicenseQueryController")
public class LicenseQueryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean LicenseQueryService licenseQueryService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    static RequestPostProcessor asAdmin() {
        return authentication(new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("MANAGE_LICENSE"))));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/license — no auth → 401")
    public void getCurrent_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/license")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no MANAGE_LICENSE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/license — no MANAGE_LICENSE → 403")
    public void getCurrent_noAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/license").with(asUser()))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — MANAGE_LICENSE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/license — MANAGE_LICENSE → 200")
    public void getCurrent_withAuthority_returns200() throws Exception {
        mockMvc.perform(get("/api/license").with(asAdmin()))
                .andExpect(status().isOk());
    }

}
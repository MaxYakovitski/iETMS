package com.mayak.ietms.features.license.api;

import com.mayak.ietms.features.license.application.LicenseCommandService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LicenseCommandController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("LicenseCommandController")
public class LicenseCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean LicenseCommandService licenseCommandService;

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
    @DisplayName("POST /api/license — no auth → 401")
    public void activate_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/license")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/license — no auth → 401")
    public void deactivate_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/license")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no MANAGE_LICENSE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/license — no MANAGE_LICENSE → 403")
    public void activate_noAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/license").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/license — no MANAGE_LICENSE → 403")
    public void deactivate_noAuthority_returns403() throws Exception {
        mockMvc.perform(delete("/api/license").with(asUser()))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — MANAGE_LICENSE
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/license — MANAGE_LICENSE → 200")
    public void activate_withAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/license").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/license — MANAGE_LICENSE → 200")
    public void deactivate_withAuthority_returns200() throws Exception {
        mockMvc.perform(delete("/api/license").with(asAdmin()))
                .andExpect(status().isOk());
    }

}
package com.mayak.ietms.features.analytics.api;

import com.mayak.ietms.features.analytics.application.AnalyticsService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("AnalyticsController")
public class AnalyticsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean AnalyticsService analyticsService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    static RequestPostProcessor asAnalyst() {
        return authentication(new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("VIEW_ANALYTICS"))));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/analytics/report — no auth → 401")
    public void getAnalytics_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/analytics/report")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/analytics/companies-for-department — no auth → 401")
    public void findCompanies_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/analytics/companies-for-department"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no VIEW_ANALYTICS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/analytics/report — no VIEW_ANALYTICS → 403")
    public void getAnalytics_noAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/analytics/report").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/analytics/companies-for-department — no VIEW_ANALYTICS → 403")
    public void findCompanies_noAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/analytics/companies-for-department").with(asUser())
                        .param("departmentId", "1")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — VIEW_ANALYTICS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/analytics/report — VIEW_ANALYTICS → 200")
    public void getAnalytics_withAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/analytics/report").with(asAnalyst())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/analytics/companies-for-department — VIEW_ANALYTICS → 200")
    public void findCompanies_withAuthority_returns200() throws Exception {
        mockMvc.perform(get("/api/analytics/companies-for-department").with(asAnalyst())
                        .param("departmentId", "1")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk());
    }
}
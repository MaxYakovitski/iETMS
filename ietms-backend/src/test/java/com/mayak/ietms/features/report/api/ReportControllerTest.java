package com.mayak.ietms.features.report.api;

import com.mayak.ietms.features.report.application.ReportExportService;
import com.mayak.ietms.features.request.application.RequestQueryService;
import com.mayak.ietms.features.statistics.application.UserStatisticsService;
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

@WebMvcTest(ReportController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("ReportController")
public class ReportControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean RequestQueryService requestQueryService;
    @MockitoBean UserStatisticsService userStatisticsService;
    @MockitoBean ReportExportService reportExportService;

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

    @Test @DisplayName("GET /api/reports/requests.xlsx — no auth → 401")
    public void exportRequests_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/requests.xlsx")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no VIEW_ANALYTICS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/reports/requests.xlsx — no VIEW_ANALYTICS → 403")
    public void exportRequests_noAuthority_returns403() throws Exception {
        mockMvc.perform(get("/api/reports/requests.xlsx").with(asUser())
                        .param("type", "BY_DEPARTMENT")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — VIEW_ANALYTICS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/reports/requests.xlsx — VIEW_ANALYTICS → 200")
    public void exportRequests_withAuthority_returns200() throws Exception {
        mockMvc.perform(get("/api/reports/requests.xlsx").with(asAnalyst())
                        .param("type", "BY_DEPARTMENT")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-31"))
                .andExpect(status().isOk());
    }
}
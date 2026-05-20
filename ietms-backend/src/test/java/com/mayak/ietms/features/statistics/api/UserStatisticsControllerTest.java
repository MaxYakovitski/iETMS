package com.mayak.ietms.features.statistics.api;

import com.mayak.ietms.features.statistics.application.UserPersonalStatisticsService;
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

@WebMvcTest(UserStatisticsController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("UserStatisticsController")
public class UserStatisticsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean UserPersonalStatisticsService statisticsService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/statistics/users/{id}/current-month — no auth → 401")
    public void getCurrentMonthStats_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/statistics/users/1/current-month"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test @DisplayName("GET /api/statistics/users/{id}/current-month — authenticated → 200")
    public void getCurrentMonthStats_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/statistics/users/1/current-month").with(asUser()))
                .andExpect(status().isOk());
    }
}
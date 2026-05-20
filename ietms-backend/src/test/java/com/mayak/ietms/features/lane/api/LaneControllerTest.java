package com.mayak.ietms.features.lane.api;

import com.mayak.ietms.features.lane.application.LaneService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaneController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("LaneController")
public class LaneControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean LaneService laneService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/lanes/by-company/{id} — no auth → 401")
    public void findByCompany_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/lanes/by-company/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/lanes/by-company/{id} — no auth → 401")
    public void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/lanes/by-company/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/lanes/{id} — no auth → 401")
    public void delete_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/lanes/1")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/lanes/by-company/{id} — authenticated → 200")
    public void findByCompany_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/lanes/by-company/1").with(asUser()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/lanes/by-company/{id} — authenticated → 200")
    public void create_authenticated_returns200() throws Exception {
        mockMvc.perform(post("/api/lanes/by-company/1").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/lanes/{id} — authenticated → 200")
    public void update_authenticated_returns200() throws Exception {
        mockMvc.perform(put("/api/lanes/1").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/lanes/{id} — authenticated → 200")
    public void delete_authenticated_returns200() throws Exception {
        mockMvc.perform(delete("/api/lanes/1").with(asUser())).andExpect(status().isOk());
    }
}
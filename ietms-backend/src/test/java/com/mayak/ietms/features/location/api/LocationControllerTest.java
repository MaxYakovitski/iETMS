package com.mayak.ietms.features.location.api;

import com.mayak.ietms.features.location.application.LocationCommandService;
import com.mayak.ietms.features.location.application.LocationQueryService;
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

@WebMvcTest(LocationController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("LocationController")
public class LocationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean LocationQueryService locationQueryService;
    @MockitoBean LocationCommandService locationCommandService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    static RequestPostProcessor asAdmin() {
        return authentication(new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("MANAGE_LOCATIONS"))));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/locations — no auth → 401")
    public void findAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/locations")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/locations — no auth → 401")
    public void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/locations")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/locations/{id} — no auth → 401")
    public void delete_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/locations/1")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no MANAGE_LOCATIONS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/locations — no MANAGE_LOCATIONS → 403")
    public void create_noAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/locations").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/locations — no MANAGE_LOCATIONS → 403")
    public void update_noAuthority_returns403() throws Exception {
        mockMvc.perform(put("/api/locations").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/locations/{id} — no MANAGE_LOCATIONS → 403")
    public void delete_noAuthority_returns403() throws Exception {
        mockMvc.perform(delete("/api/locations/1").with(asUser()))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated / MANAGE_LOCATIONS
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/locations — authenticated → 200")
    public void findAll_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/locations").with(asUser())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/locations — MANAGE_LOCATIONS → 200")
    public void create_withAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/locations").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/locations — MANAGE_LOCATIONS → 200")
    public void update_withAuthority_returns200() throws Exception {
        mockMvc.perform(put("/api/locations").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/locations/{id} — MANAGE_LOCATIONS → 200")
    public void delete_withAuthority_returns200() throws Exception {
        mockMvc.perform(delete("/api/locations/1").with(asAdmin()))
                .andExpect(status().isOk());
    }
}
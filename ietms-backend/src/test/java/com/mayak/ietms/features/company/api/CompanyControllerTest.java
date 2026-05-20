package com.mayak.ietms.features.company.api;

import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.company.application.CompanyService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("CompanyController")
public class CompanyControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean CompanyService companyService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    static RequestPostProcessor asAdmin() {
        return authentication(new UsernamePasswordAuthenticationToken(
                1L, null, List.of(new SimpleGrantedAuthority("MANAGE_CRM"))));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/companies — no auth → 401")
    public void findAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/companies")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/companies — no auth → 401")
    public void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/companies")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} — no auth → 401")
    public void delete_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/companies/1")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — authenticated, no MANAGE_CRM
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/companies — no MANAGE_CRM → 403")
    public void create_noAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/companies").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/companies/{id} — no MANAGE_CRM → 403")
    public void update_noAuthority_returns403() throws Exception {
        mockMvc.perform(put("/api/companies/1").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} — no MANAGE_CRM → 403")
    public void delete_noAuthority_returns403() throws Exception {
        mockMvc.perform(delete("/api/companies/1").with(asUser()))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated / MANAGE_CRM
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/companies — authenticated → 200")
    public void findAll_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/companies").with(asUser())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/companies/by-name — authenticated → 200")
    public void findByName_authenticated_returns200() throws Exception {
        given(companyService.findByName(anyString())).willReturn(Optional.of(new CompanyDto(1L, "Test")));
        mockMvc.perform(get("/api/companies/by-name").param("name", "Test").with(asUser()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/companies — MANAGE_CRM → 200")
    public void create_withAuthority_returns200() throws Exception {
        mockMvc.perform(post("/api/companies").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/companies/{id} — MANAGE_CRM → 200")
    public void update_withAuthority_returns200() throws Exception {
        mockMvc.perform(put("/api/companies/1").with(asAdmin())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/companies/{id} — MANAGE_CRM → 200")
    public void delete_withAuthority_returns200() throws Exception {
        mockMvc.perform(delete("/api/companies/1").with(asAdmin()))
                .andExpect(status().isOk());
    }

}
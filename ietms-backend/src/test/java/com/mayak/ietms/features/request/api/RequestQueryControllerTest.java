package com.mayak.ietms.features.request.api;

import com.mayak.ietms.features.request.application.RequestQueryService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestQueryController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("RequestQueryController")
public class RequestQueryControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean SlackAlertService slackAlertService;

    @MockitoBean RequestQueryService requestQueryService;
    @MockitoBean UserQueryService userQueryService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────


    @Test
    @DisplayName("GET /api/requests — no auth → 401")
    public void findPage_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/requests")
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/requests/filter — no auth → 401")
    public void filter_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/requests/filter")
                        .param("page", "0").param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/requests/{id} — no auth → 401")
    public void getDetails_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/requests/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/requests — authenticated → 200")
    public void findPage_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/requests")
                        .param("page", "0").param("size", "10")
                        .with(asUser()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/requests/filter — authenticated → 200")
    public void filter_authenticated_returns200() throws Exception {
        mockMvc.perform(post("/api/requests/filter")
                        .param("page", "0").param("size", "10")
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/requests/{id} — authenticated → 200")
    public void getDetails_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/requests/1").with(asUser()))
                .andExpect(status().isOk());
    }
}
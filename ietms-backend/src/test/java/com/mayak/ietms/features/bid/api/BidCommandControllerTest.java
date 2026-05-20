package com.mayak.ietms.features.bid.api;

import com.mayak.ietms.features.bid.application.BidCommandService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BidCommandController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("BidCommandController")
public class BidCommandControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;
    @MockitoBean SlackAlertService slackAlertService;

    @MockitoBean BidCommandService bidCommandService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/bids — no auth → 401")
    public void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/bids"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/bids/{id} — no auth → 401")
    public void delete_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/bids/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/bids — authenticated → 200")
    public void create_authenticated_returns200() throws Exception {
        mockMvc.perform(post("/api/bids").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":1,\"amount\":500.00}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/bids/{id} — authenticated → 200")
    public void delete_authenticated_returns200() throws Exception {
        mockMvc.perform(delete("/api/bids/1").with(asUser()))
                .andExpect(status().isOk());
    }
}
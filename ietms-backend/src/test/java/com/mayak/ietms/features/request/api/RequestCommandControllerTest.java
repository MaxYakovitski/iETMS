package com.mayak.ietms.features.request.api;

import com.mayak.ietms.features.request.application.RequestCommandService;
import com.mayak.ietms.features.request.application.assembly.RequestDetailsAssembler;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.infrastructure.notify.SlackAlertService;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RequestCommandController.class)
@DisplayName("RequestCommandController")
public class RequestCommandControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean RequestCommandService requestCommandService;
    @MockitoBean UserQueryService userQueryService;
    @MockitoBean SlackAlertService slackAlertService;

    @MockitoBean RequestDetailsAssembler requestDetailsAssembler;

    static org.springframework.test.web.servlet.request.RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/requests/{id}/join — no auth → 401")
    public void join_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/requests/1/join").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/requests/{id} — no auth → 401")
    public void delete_noAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/requests/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/requests/{id}/accept — no auth → 401")
    public void accept_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/requests/1/accept").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/requests/{id}/join — authenticated → 204")
    public void join_authenticated_returns204() throws Exception {
        mockMvc.perform(post("/api/requests/1/join")
                        .with(csrf())
                        .with(asUser())
                )
                .andExpect(status().isNoContent());
        verify(requestCommandService).join(1L, 1L);
    }

    @Test
    @DisplayName("DELETE /api/requests/{id} — authenticated → 204")
    public void delete_authenticated_returns204() throws Exception {
        mockMvc.perform(delete("/api/requests/1")
                        .with(csrf())
                        .with(asUser())
                )
                .andExpect(status().isNoContent());
        verify(requestCommandService).delete(1L, 1L);
    }

    @Test
    @DisplayName("POST /api/requests/{id}/accept — authenticated, nobody → 204")
    public void accept_authenticated_noBody_returns204() throws Exception {
        mockMvc.perform(post("/api/requests/1/accept")
                        .with(csrf())
                        .with(asUser())
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
        verify(requestCommandService).accept(1L, null, 1L);
    }
}
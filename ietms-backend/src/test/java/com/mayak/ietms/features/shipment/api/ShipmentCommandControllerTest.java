package com.mayak.ietms.features.shipment.api;

import com.mayak.ietms.features.shipment.application.ShipmentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentCommandController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("ShipmentCommandController")
public class ShipmentCommandControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean UserRepository userRepository;

    @MockitoBean JwtService jwtService;
    @MockitoBean SlackAlertService slackAlertService;
    @MockitoBean ShipmentService shipmentService;

    static RequestPostProcessor asUser() {
        return authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()));
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — unauthenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/shipments/{id}/cancel — no auth → 401")
    public void cancel_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/shipments/1/cancel")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/shipments/{id} — no auth → 401")
    public void update_noAuth_returns401() throws Exception {
        mockMvc.perform(patch("/api/shipments/1")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/shipments/{id}/cancel — authenticated → 200")
    public void cancel_authenticated_returns200() throws Exception {
        mockMvc.perform(post("/api/shipments/1/cancel").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"CANCELLED_BY_US\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/shipments/{id} — authenticated → 200")
    public void update_authenticated_returns200() throws Exception {
        mockMvc.perform(patch("/api/shipments/1").with(asUser())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

}
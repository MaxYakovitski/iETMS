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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShipmentQueryController.class)
@Import({SecurityConfig.class, RestAuthenticationEntryPoint.class})
@DisplayName("ShipmentQueryController")
public class ShipmentQueryControllerTest {

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
    @DisplayName("GET /api/shipments/my-transports — no auth → 401")
    public void myTransports_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/shipments/my-transports")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/shipments/my-shipments — no auth → 401")
    public void findMyShipments_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/shipments/my-shipments")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/shipments/{id} — no auth → 401")
    public void getDetails_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/shipments/1")).andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────
    // Happy path — authenticated
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/shipments/my-transports — authenticated → 200")
    public void myTransports_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/shipments/my-transports").with(asUser()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/shipments/my-shipments — authenticated → 200")
    public void findMyShipments_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/shipments/my-shipments").param("date", "2026-01-01").with(asUser()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/shipments/{id} — authenticated → 200")
    public void getDetails_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/api/shipments/1").with(asUser())).andExpect(status().isOk());
    }
}
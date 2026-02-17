package com.mayak.iet.integration.bridge;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/bridge")
@RequiredArgsConstructor
public class DesktopBridgeController {

    private final JwtTokenProvider tokenProvider;

    @GetMapping("/token")
    public ResponseEntity<?> token() {
        return tokenProvider.getToken()
                .map(token -> ResponseEntity.ok(Map.of("token", token)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
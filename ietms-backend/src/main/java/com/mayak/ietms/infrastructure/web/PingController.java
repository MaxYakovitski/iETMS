package com.mayak.ietms.infrastructure.web;

import com.mayak.ietms.infrastructure.security.auth.persistence.RefreshTokenRepository;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@Hidden
@RequiredArgsConstructor
public class PingController {

    private final RefreshTokenRepository refreshTokenRepository;

    @RequestMapping("/ping")
    public void ping(@AuthenticationPrincipal Long userId, HttpServletRequest request) {
        if (userId == null) return;
        Long sessionId = (Long) request.getAttribute("sessionId");
        if (sessionId == null || !refreshTokenRepository.existsByIdAndRevokedFalse(sessionId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalidated");
        }
    }
}
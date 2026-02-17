package com.mayak.ietms.infrastructure.security.auth.dto;

public record LoginRequest(
        String email,
        String password) {
}
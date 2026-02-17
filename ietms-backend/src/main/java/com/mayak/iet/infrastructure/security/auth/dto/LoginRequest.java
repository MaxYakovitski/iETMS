package com.mayak.iet.infrastructure.security.auth.dto;

public record LoginRequest(
        String email,
        String password) {
}
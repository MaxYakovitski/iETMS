package com.mayak.iet.integration.auth.dto;

public record LoginRequestDto(
        String email,
        String password
) {
}

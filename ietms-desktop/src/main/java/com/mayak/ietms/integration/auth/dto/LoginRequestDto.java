package com.mayak.ietms.integration.auth.dto;

public record LoginRequestDto(
        String email,
        String password
) {
}

package com.mayak.iet.common.dto.error;

import java.util.Map;

public record ErrorResponseDto(
        String message,
        Map<String, String> errors) {
}
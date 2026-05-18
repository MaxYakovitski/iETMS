package com.mayak.ietms.common.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(String code, String message, Map<String, String> errors) {

    public ErrorResponseDto(String code, String message) {
        this(code, message, null);
    }
}
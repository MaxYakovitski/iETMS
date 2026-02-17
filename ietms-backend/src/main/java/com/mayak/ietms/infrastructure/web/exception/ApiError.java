package com.mayak.ietms.infrastructure.web.exception;

public record ApiError(
        String code,
        String message) {
}
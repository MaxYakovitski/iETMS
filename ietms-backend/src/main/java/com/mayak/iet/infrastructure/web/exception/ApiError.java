package com.mayak.iet.infrastructure.web.exception;

public record ApiError(
        String code,
        String message) {
}
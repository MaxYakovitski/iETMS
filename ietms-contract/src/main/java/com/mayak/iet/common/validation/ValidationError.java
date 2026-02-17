package com.mayak.iet.common.validation;

public record ValidationError(
        String code,
        String message) {
}
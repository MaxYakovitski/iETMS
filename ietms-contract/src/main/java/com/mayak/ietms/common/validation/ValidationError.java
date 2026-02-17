package com.mayak.ietms.common.validation;

public record ValidationError(
        String code,
        String message) {
}
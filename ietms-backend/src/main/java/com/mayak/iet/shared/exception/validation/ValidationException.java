package com.mayak.iet.shared.exception.validation;

import com.mayak.iet.common.validation.ValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationException extends RuntimeException {
    private final ValidationResult result;
}
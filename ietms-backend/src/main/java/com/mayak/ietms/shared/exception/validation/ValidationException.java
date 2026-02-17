package com.mayak.ietms.shared.exception.validation;

import com.mayak.ietms.common.validation.ValidationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidationException extends RuntimeException {
    private final ValidationResult result;
}
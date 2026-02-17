package com.mayak.ietms.user.validator;

import com.mayak.ietms.common.validation.ValidationResult;

public abstract class AbstractContractValidator {
    protected void required(Object value, String field, ValidationResult result) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            result.add(field, field + " is required");
        }
    }
}
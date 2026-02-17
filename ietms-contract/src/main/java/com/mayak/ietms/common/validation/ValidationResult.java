package com.mayak.ietms.common.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult {

    private final List<ValidationError> errors = new ArrayList<>();
    public void add(String code, String message) {
        errors.add(new ValidationError(code, message));
    }
    public void add(ValidationError error) {
        errors.add(error);
    }
    public boolean isValid() {
        return errors.isEmpty();
    }

}
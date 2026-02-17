package com.mayak.ietms.common.validation;

public interface Validator <T>{
    ValidationResult isValid(T object);
}
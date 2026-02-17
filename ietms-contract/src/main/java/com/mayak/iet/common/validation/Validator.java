package com.mayak.iet.common.validation;

public interface Validator <T>{
    ValidationResult isValid(T object);
}
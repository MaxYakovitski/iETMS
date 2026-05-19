package com.mayak.ietms.shared.exception.business;

public class DepartmentInUseException extends RuntimeException {
    public DepartmentInUseException(String reason) {
        super(reason);
    }
}
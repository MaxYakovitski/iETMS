package com.mayak.ietms.shared.exception.business;

public class DepartmentInUseException extends RuntimeException {
    public DepartmentInUseException(Long id) {
        super("Department is used: " + id);
    }
}
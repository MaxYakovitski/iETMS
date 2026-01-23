package com.mayak.iet.shared.exception.business;

public class DepartmentInUseException extends RuntimeException {
    public DepartmentInUseException(Long id) {
        super("Department is used: " + id);
    }
}
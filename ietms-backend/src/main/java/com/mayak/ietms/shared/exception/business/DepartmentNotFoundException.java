package com.mayak.ietms.shared.exception.business;

public class DepartmentNotFoundException extends RuntimeException  {

    public DepartmentNotFoundException(Long id) {
        super("Department: " + id + " not found!");
    }
}

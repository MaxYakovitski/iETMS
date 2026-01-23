package com.mayak.iet.shared.exception.business;

public class DepartmentNotFoundException extends RuntimeException  {
    public DepartmentNotFoundException(Long id) {super("Department not found: " + id);}
}

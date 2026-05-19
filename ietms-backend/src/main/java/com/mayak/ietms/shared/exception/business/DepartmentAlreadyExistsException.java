package com.mayak.ietms.shared.exception.business;

public class DepartmentAlreadyExistsException extends RuntimeException{
    public DepartmentAlreadyExistsException(String code, String name) {
        super("Department already exists with code='" + code + "' or name='" + name + "'");
    }
}
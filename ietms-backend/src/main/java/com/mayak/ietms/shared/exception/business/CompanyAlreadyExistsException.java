package com.mayak.ietms.shared.exception.business;

public class CompanyAlreadyExistsException extends RuntimeException {
    public CompanyAlreadyExistsException(String code, String name) {
        super("Company already exists with code='" + code + "' or name='" + name + "'");
    }
}
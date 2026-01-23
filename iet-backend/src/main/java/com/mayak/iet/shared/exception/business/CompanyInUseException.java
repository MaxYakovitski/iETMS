package com.mayak.iet.shared.exception.business;

public class CompanyInUseException extends RuntimeException {
    public CompanyInUseException(Long id) {
        super("Company is used: " + id);
    }
}
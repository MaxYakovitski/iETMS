package com.mayak.ietms.shared.exception.business;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(Long id) {
        super("Company not found: " + id);
    }
}
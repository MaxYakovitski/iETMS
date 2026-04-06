package com.mayak.ietms.shared.exception.business;

public class CompanyInUseException extends RuntimeException {
    public CompanyInUseException(Long id) {
        super("Company is used: " + id);
    }

    public CompanyInUseException(String reason) {
        super(reason);
    }
}
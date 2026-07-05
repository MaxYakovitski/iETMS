package com.mayak.ietms.shared.exception.business;

public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(Long id) {
        super("Company: " + id + " not found!");
    }

    public CompanyNotFoundException(String name) {
        super("Company: "  + name + " not found!");
    }
}
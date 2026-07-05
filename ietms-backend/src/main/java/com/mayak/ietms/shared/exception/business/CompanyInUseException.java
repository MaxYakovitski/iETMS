package com.mayak.ietms.shared.exception.business;

public class CompanyInUseException extends RuntimeException {

    public CompanyInUseException(String reason) {
        super(reason);
    }
}

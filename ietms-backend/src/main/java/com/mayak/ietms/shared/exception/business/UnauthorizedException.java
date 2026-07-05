package com.mayak.ietms.shared.exception.business;

public class UnauthorizedException extends  RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}

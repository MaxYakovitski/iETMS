package com.mayak.ietms.shared.exception.business;

public class RequestNotFoundException extends  RuntimeException {
    public RequestNotFoundException(Long id) {
            super("Request not found: " + id);
        }
}
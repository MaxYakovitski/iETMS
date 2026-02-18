package com.mayak.ietms.integration.exception;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException() {
        super("Session expired");
    }
}
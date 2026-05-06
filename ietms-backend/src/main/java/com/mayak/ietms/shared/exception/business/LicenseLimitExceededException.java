package com.mayak.ietms.shared.exception.business;

/** Thrown when the active license's user limit has been reached. */
public class LicenseLimitExceededException extends RuntimeException{
    public LicenseLimitExceededException(String message) {
        super(message);
    }
}
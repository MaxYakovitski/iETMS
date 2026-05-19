package com.mayak.ietms.shared.exception.business;

public class UserInUseException extends RuntimeException {
    public UserInUseException(String reason) {
        super(reason);
    }
}
package com.mayak.iet.shared.exception.business;

public class UserInUseException extends RuntimeException {
    public UserInUseException(Long id) {
        super("User is used: " + id);
    }
}

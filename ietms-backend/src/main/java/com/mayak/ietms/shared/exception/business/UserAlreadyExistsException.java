package com.mayak.ietms.shared.exception.business;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException (String code, String name) {
        super("User already exists with code='" + code + "' or name='" + name + "'");
    }
}

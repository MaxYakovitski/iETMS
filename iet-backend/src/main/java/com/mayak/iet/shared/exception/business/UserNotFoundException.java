package com.mayak.iet.shared.exception.business;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) { super("User not found: " + id);}
}

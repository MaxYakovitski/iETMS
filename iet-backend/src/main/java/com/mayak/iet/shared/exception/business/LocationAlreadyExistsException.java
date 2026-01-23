package com.mayak.iet.shared.exception.business;

public class LocationAlreadyExistsException extends RuntimeException {
    public LocationAlreadyExistsException(String code, String name) {
        super("Lane already exists with code='" + code + "' or name='" + name + "'");
    }
}
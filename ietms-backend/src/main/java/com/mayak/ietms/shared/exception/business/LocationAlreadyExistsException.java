package com.mayak.ietms.shared.exception.business;

public class LocationAlreadyExistsException extends RuntimeException {

    public LocationAlreadyExistsException(String code, String name) {
        super("Location already exists with code='" + code + "' or name='" + name + "'");
    }
}

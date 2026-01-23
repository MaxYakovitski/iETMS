package com.mayak.iet.shared.exception.business;

public class LocationInUseException extends RuntimeException {
    public LocationInUseException(Long id) {
        super("Location is used: " + id);
    }
}
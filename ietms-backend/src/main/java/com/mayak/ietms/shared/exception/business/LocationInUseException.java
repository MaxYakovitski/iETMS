package com.mayak.ietms.shared.exception.business;

public class LocationInUseException extends RuntimeException {
    public LocationInUseException(String reason) {
        super(reason);
    }
}
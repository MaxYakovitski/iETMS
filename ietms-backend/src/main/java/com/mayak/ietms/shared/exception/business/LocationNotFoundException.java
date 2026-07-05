package com.mayak.ietms.shared.exception.business;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(Long id) {
        super("Location: " + id + " not found!");
    }
}

package com.mayak.iet.shared.exception.business;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(Long id) {super("Location not found: " + id);}
}
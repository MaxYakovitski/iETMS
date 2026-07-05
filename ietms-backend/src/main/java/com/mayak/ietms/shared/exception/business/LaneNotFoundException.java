package com.mayak.ietms.shared.exception.business;

public class LaneNotFoundException extends RuntimeException {

    public LaneNotFoundException(Long id) {
        super("Lane: " + id +  " not found!");
    }
}

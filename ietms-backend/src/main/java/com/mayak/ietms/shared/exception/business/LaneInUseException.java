package com.mayak.ietms.shared.exception.business;

public class LaneInUseException extends RuntimeException {
    public LaneInUseException(Long id) {
        super("Lane is used: " + id);
    }
}

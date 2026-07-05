package com.mayak.ietms.shared.exception.business;

public class LaneAlreadyExistsException extends RuntimeException {

    public LaneAlreadyExistsException(String code, String name) {
        super("Lane already exists with code='" + code + "' or name='" + name + "'");
    }
}

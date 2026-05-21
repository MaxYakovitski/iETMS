package com.mayak.ietms.shared.exception.business;

public class AlreadyJoinedException extends RuntimeException{
    public AlreadyJoinedException(Long requestId) {
        super("User already joined request " + requestId);
    }
}
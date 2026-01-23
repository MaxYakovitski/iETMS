package com.mayak.iet.shared.exception.business;

public class RequestDeletionNotAllowedException extends RuntimeException{
    public RequestDeletionNotAllowedException(Long requestId) {
        super("Cannot delete request " + requestId + " because a shipment already exists");
    }
}
package com.mayak.ietms.shared.exception.business;

public class RequestDeletionNotAllowedException extends RuntimeException{

    public RequestDeletionNotAllowedException(Long requestId) {
        super("Cannot delete request: " + requestId + " because the shipment for it already exists!");
    }
}

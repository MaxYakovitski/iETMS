package com.mayak.ietms.shared.exception.business;

public class NoActiveBidsException extends  RuntimeException {
    public NoActiveBidsException(Long requestId) {
        super("Cannot accept request " + requestId + " because it has no active bids");
    }
}
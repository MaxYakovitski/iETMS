package com.mayak.ietms.shared.exception.business;

public class BidNotFoundException extends RuntimeException {
    public BidNotFoundException(Long id) {super("Bid not found: " + id);}
}
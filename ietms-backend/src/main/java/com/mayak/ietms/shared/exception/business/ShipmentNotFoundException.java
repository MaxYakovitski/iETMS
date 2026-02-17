package com.mayak.ietms.shared.exception.business;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(Long id) {super("Shipment not found: " + id);}
}
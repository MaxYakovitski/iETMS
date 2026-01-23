package com.mayak.iet.shared.exception.business;

public class ShipmentCancellationNotAllowedException extends  RuntimeException {
    public ShipmentCancellationNotAllowedException(Long shipmentId) {
        super("Cannot cancel shipment " + shipmentId + " because it is already in a final state");
    }
}
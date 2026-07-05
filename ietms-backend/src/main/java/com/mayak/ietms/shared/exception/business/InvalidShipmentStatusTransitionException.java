package com.mayak.ietms.shared.exception.business;

import com.mayak.ietms.features.shipment.domain.enums.ShipmentStatus;

public class InvalidShipmentStatusTransitionException extends RuntimeException {

    public InvalidShipmentStatusTransitionException(ShipmentStatus from, ShipmentStatus to) {
        super("Invalid shipment status transition: " + from + " -> " + to + "!");
    }
}

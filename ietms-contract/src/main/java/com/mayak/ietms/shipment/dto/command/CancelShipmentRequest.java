package com.mayak.ietms.shipment.dto.command;

import com.mayak.ietms.shipment.dto.enums.ShipmentCancelReasonDto;

public record CancelShipmentRequest(
        ShipmentCancelReasonDto reason) {
}
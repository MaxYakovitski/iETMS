package com.mayak.iet.shipment.dto.command;

import com.mayak.iet.shipment.dto.enums.ShipmentCancelReasonDto;

public record CancelShipmentRequest(
        ShipmentCancelReasonDto reason) {
}
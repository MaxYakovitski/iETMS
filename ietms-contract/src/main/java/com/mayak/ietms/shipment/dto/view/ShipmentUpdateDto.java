package com.mayak.ietms.shipment.dto.view;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;

import java.time.Instant;

public record ShipmentUpdateDto(
        Long shipmentId,
        String carrierName,
        String shipmentComments,
        ShipmentStatusDto status,
        String licensePlate,
        String transportOrder,
        Instant statusAt) {
}
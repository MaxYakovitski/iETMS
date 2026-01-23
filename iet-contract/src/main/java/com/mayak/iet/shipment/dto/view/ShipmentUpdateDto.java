package com.mayak.iet.shipment.dto.view;

import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;

import java.time.LocalDateTime;

public record ShipmentUpdateDto(
        Long shipmentId,
        String carrierName,
        String shipmentComments,
        ShipmentStatusDto status,
        String licensePlate,
        String transportOrder,
        LocalDateTime statusAt) {
}
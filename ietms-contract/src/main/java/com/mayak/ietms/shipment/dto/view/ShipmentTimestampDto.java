package com.mayak.ietms.shipment.dto.view;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;

import java.time.Instant;

public record ShipmentTimestampDto(
        ShipmentStatusDto status,
        Instant at) {
}
package com.mayak.ietms.shipment.dto.view;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;

import java.time.LocalDateTime;

public record ShipmentTimestampDto(
        ShipmentStatusDto status,
        LocalDateTime at) {
}
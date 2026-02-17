package com.mayak.iet.shipment.dto.view;

import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;

import java.time.LocalDateTime;

public record ShipmentTimestampDto(
        ShipmentStatusDto status,
        LocalDateTime at) {
}
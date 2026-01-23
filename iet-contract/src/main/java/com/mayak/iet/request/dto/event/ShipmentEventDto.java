package com.mayak.iet.request.dto.event;

public record ShipmentEventDto(
        Long shipmentId,
        Long requestId,
        Long dispatcherId,
        String status) {
}
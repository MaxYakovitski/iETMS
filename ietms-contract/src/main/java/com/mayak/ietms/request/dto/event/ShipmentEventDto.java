package com.mayak.ietms.request.dto.event;

public record ShipmentEventDto(
        Long shipmentId,
        Long requestId,
        Long dispatcherId,
        String status) {
}
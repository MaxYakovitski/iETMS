package com.mayak.ietms.shipment.dto.view;

import com.mayak.ietms.shipment.dto.enums.TransportEventType;

import java.time.LocalDateTime;

public record MyTransportEventDto(
        Long shipmentId,
        TransportEventType eventType,
        LocalDateTime eventAt,
        ShipmentListItemDto shipment) {
}
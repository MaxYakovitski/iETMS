package com.mayak.ietms.shipment.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor(force = true)
public class ShipmentEvent <T> {

    public enum EventType {
        STATUS_CHANGED,
        UPDATED
    }

    private final Long shipmentId;
    private final EventType type;
    private final long version;
    private final T payload;
    private final Instant occurredAt;

    public ShipmentEvent(Long shipmentId, EventType type, long version, T payload) {
        this.shipmentId = shipmentId;
        this.type = type;
        this.version = version;
        this.payload = payload;
        this.occurredAt = Instant.now();
    }
}

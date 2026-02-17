package com.mayak.ietms.features.shipment.domain.enums;

import java.util.Set;

public enum ShipmentStatus {
    PLANNED,
    LOADED,
    DROPPED,
    CANCELED;

    public static Set<ShipmentStatus> toLoadStatuses() {
        return Set.of(PLANNED, CANCELED);
    }

    public static Set<ShipmentStatus> toDropStatuses() {
        return Set.of(LOADED, DROPPED);
    }

    public boolean canTransitionTo(ShipmentStatus next) {
        return switch (this) {
            case PLANNED -> next == LOADED || next == CANCELED;
            case LOADED -> next == DROPPED || next == CANCELED;
            default -> false;
        };
    }

    public boolean isFinal() {
        return this == DROPPED || this == CANCELED;
    }
}
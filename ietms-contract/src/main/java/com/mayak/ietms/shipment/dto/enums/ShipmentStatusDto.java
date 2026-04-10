package com.mayak.ietms.shipment.dto.enums;

public enum ShipmentStatusDto {
    NEW,
    PLANNED,
    TO_LOAD,
    LOADED,
    TO_DROP,
    DROPPED,
    CANCELED;

    public boolean isPreLoad() {
        return this == NEW || this == PLANNED || this == TO_LOAD;
    }

    public boolean isFinal() {
        return this == DROPPED || this == CANCELED;
    }
}
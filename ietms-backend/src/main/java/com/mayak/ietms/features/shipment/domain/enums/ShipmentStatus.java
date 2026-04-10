package com.mayak.ietms.features.shipment.domain.enums;

public enum ShipmentStatus {
    NEW,
    PLANNED,
    TO_LOAD,
    LOADED,
    TO_DROP,
    DROPPED,
    CANCELED;

    public boolean canTransitionTo(ShipmentStatus next, TransitionInitiator initiator) {
        return switch (initiator) {
            case USER -> switch (this) {
                case NEW              -> next == PLANNED || next == TO_LOAD || next == CANCELED;
                case PLANNED, TO_LOAD -> next == NEW || next == LOADED || next == CANCELED;
                case LOADED, TO_DROP  -> next == DROPPED;
                default               -> false;
            };
            case SYSTEM -> switch (this) {
                case PLANNED -> next == TO_LOAD;
                case LOADED  -> next == TO_DROP;
                default      -> false;
            };
        };
    }

    public boolean hasTimestamp() {
        return this == NEW || this == PLANNED || this == LOADED || this == DROPPED || this == CANCELED;
    }

    public boolean isFinal() {
        return this == DROPPED || this == CANCELED;
    }
}
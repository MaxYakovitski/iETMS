package com.mayak.ietms.features.shipment.domain.enums;

/**
 * Represents the lifecycle status of a shipment.
 *
 * <p>Status transition rules:
 * <pre>
 * USER-initiated:
 *   NEW              → PLANNED, TO_LOAD, CANCELED
 *   PLANNED, TO_LOAD → NEW, LOADED, CANCELED
 *   LOADED           → TO_DROP, DROPPED
 *   TO_DROP          → DROPPED
 *
 * SYSTEM-initiated:
 *   PLANNED → TO_LOAD  (when planned load date is today)
 *   LOADED  → TO_DROP  (when planned drop date is today)
 * </pre>
 */
public enum ShipmentStatus {

    /**
     * Shipment has been created but not yet planned.
     */
    NEW,

    /**
     * Shipment has been planned — transport details are assigned.
     */
    PLANNED,

    /**
     * Shipment is ready for loading today.
     */
    TO_LOAD,

    /**
     * Shipment has been loaded onto the vehicle.
     */
    LOADED,

    /**
     * Shipment is ready for delivery today.
     */
    TO_DROP,

    /**
     * Shipment has been delivered to the destination.
     */
    DROPPED,

    /**
     * Shipment has been canceled and will not be executed.
     */
    CANCELED;

    /**
     * Returns whether this status can transition to {@code next}
     * given the {@code initiator} (user action or system scheduler).
     */
    public boolean canTransitionTo(ShipmentStatus next, TransitionInitiator initiator) {
        return switch (initiator) {
            case USER -> switch (this) {
                case NEW              -> next == PLANNED || next == TO_LOAD || next == CANCELED;
                case PLANNED, TO_LOAD -> next == NEW || next == LOADED || next == CANCELED;
                case LOADED           -> next == TO_DROP || next == DROPPED;
                case TO_DROP          -> next == DROPPED;
                default               -> false;
            };
            case SYSTEM -> switch (this) {
                case PLANNED -> next == TO_LOAD;
                case LOADED  -> next == TO_DROP;
                default      -> false;
            };
        };
    }

    /**
     * Returns whether this status records a timestamp when entered.
     * TO_LOAD and TO_DROP are intermediate system states and do not record timestamps.
     */
    public boolean hasTimestamp() {
        return this == NEW || this == PLANNED || this == LOADED || this == DROPPED || this == CANCELED;
    }

    /**
     * Returns whether this status is terminal — no further transitions are allowed.
     */
    public boolean isFinal() {
        return this == DROPPED || this == CANCELED;
    }
}
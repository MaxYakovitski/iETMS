package com.mayak.iet.features.request.domain.enums;

public enum RequestStatus {
    NEW,
    IN_PROGRESS,
    BIDDING,
    OFFERED,
    ACCEPTED,
    REFUSED;

    public boolean isFinal() {
        return this == ACCEPTED || this == REFUSED;
    }

    public boolean allowsBidding() {
        return switch (this) {
            case NEW, IN_PROGRESS, BIDDING, OFFERED -> true;
            default -> false;
        };
    }

    public boolean canTransitionTo(RequestStatus next) {
        return switch (this) {
            case NEW -> next == IN_PROGRESS || next == BIDDING;
            case IN_PROGRESS -> next == BIDDING;
            case BIDDING -> next == OFFERED;
            case OFFERED -> next == ACCEPTED || next == REFUSED;
            default -> false;
        };
    }
}
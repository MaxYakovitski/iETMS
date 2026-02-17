package com.mayak.ietms.features.request.domain.enums;

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
}
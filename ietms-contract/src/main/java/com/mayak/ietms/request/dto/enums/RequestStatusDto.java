package com.mayak.ietms.request.dto.enums;

public enum RequestStatusDto {
    NEW,
    IN_PROGRESS,
    BIDDING,
    OFFERED,
    ACCEPTED,
    REFUSED;

    public boolean isFinal() {
        return this == ACCEPTED || this == REFUSED;
    }
}
package com.mayak.ietms.request.dto.enums;

import lombok.Getter;

@Getter
public enum SpotReasonCodeDto {
    OFFERED_NOT_ON_TIME("Offered not on time"),
    BAD_TRANSIT_TIME("Bad transit time");

    private final String label;

    SpotReasonCodeDto(String label) {
        this.label = label;}
}
package com.mayak.iet.features.request.domain.enums;

import com.mayak.iet.features.request.domain.model.RefuseReason;
import lombok.Getter;

@Getter
public enum SpotReasonCode implements RefuseReason {
    OFFERED_NOT_ON_TIME("OFFERED_NOT_ON_TIME", "offered not on time"),
    BAD_TRANSIT_TIME("BAD_TRANSIT_TIME", "bad transit time");

    private final String code;
    private final String label;

    SpotReasonCode(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
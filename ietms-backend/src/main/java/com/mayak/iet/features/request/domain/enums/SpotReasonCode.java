package com.mayak.iet.features.request.domain.enums;

import com.mayak.iet.features.request.domain.model.RefuseReason;
import lombok.Getter;

@Getter
public enum SpotReasonCode implements RefuseReason {
    OFFERED_NOT_ON_TIME,
    BAD_TRANSIT_TIME;

    @Override
    public String getCode() {
        return name();
    }
}
package com.mayak.iet.features.request.domain.enums;

import com.mayak.iet.features.request.domain.model.RefuseReason;
import lombok.Getter;

@Getter
public enum ReasonCode implements RefuseReason {

    BID_NOT_PROVIDED { @Override public boolean isUserSelectable() { return false; } },
    PRICE_NOT_ACCEPTABLE;

    @Override
    public String getCode() {
        return name();
    }
}
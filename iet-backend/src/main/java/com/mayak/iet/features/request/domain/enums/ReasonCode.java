package com.mayak.iet.features.request.domain.enums;

import com.mayak.iet.features.request.domain.model.RefuseReason;
import lombok.Getter;

@Getter
public enum ReasonCode implements RefuseReason {

    BID_NOT_PROVIDED("BID_NOT_PROVIDED", "bid not provided") {
        @Override
        public boolean isUserSelectable() {
            return false;
        }
    },
    PRICE_NOT_ACCEPTABLE("PRICE_NOT_ACCEPTABLE", "price not acceptable");

    private final String code;
    private final String label;

    ReasonCode(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
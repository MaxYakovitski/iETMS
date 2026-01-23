package com.mayak.iet.features.request.domain.enums;

import com.mayak.iet.features.request.domain.model.RefuseReason;
import lombok.Getter;

@Getter
public enum ContractReasonCode implements RefuseReason {
    NO_CORRESPONDING_TRUCK("NO_CORRESPONDING_TRUCK", "no corresponding truck");

    private final String code;
    private final String label;

    ContractReasonCode(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
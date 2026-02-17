package com.mayak.ietms.features.request.domain.enums;

import com.mayak.ietms.features.request.domain.model.RefuseReason;
import lombok.Getter;

@Getter
public enum ContractReasonCode implements RefuseReason {
    NO_CORRESPONDING_TRUCK;

    @Override
    public String getCode() {
        return name();
    }
}
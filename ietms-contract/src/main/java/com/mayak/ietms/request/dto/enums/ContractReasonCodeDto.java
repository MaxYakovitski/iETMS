package com.mayak.ietms.request.dto.enums;

import lombok.Getter;

@Getter
public enum ContractReasonCodeDto {
    NO_CORRESPONDING_TRUCK("No corresponding truck");

    private final String label;

    ContractReasonCodeDto(String label) {
        this.label = label;
    }
}
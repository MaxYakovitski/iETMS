package com.mayak.ietms.ui.workspace.request.client;

import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.request.dto.create.BaseRequestDto;

public record SubmitResult(BaseRequestDto dto, ValidationResult validation) {
    public boolean isValid() {
        return validation.isValid();
    }
}
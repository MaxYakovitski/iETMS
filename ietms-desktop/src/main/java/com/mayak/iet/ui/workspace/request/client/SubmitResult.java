package com.mayak.iet.ui.workspace.request.client;

import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.request.dto.create.BaseRequestDto;

public record SubmitResult(BaseRequestDto dto, ValidationResult validation) {
    public boolean isValid() {
        return validation.isValid();
    }
}
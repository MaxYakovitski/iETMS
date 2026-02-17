package com.mayak.ietms.ui.workspace.request.client;

import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.infrastructure.assembler.ClientRequestAssembler;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.validator.RequestContractValidator;
import com.mayak.ietms.ui.workspace.request.form.ClientRequestFormState;

public class ClientRequestSubmitService {

    private final RequestContractValidator validator = new RequestContractValidator();

    public SubmitResult prepare(ClientRequestFormState state) {
        BaseRequestDto dto = ClientRequestAssembler.build(state);
        normalizeCompany(dto);

        ValidationResult validation = validator.isValid(dto);
        return new SubmitResult(dto, validation);
    }

    private void normalizeCompany(BaseRequestDto dto) {
        if (dto == null) return;
        dto.setCustomerName(TextUtils.safeTrim(dto.getCustomerName()));
    }
}

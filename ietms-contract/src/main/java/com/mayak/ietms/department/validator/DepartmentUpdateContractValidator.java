package com.mayak.ietms.department.validator;

import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.user.validator.AbstractContractValidator;

public class DepartmentUpdateContractValidator extends AbstractContractValidator implements Validator<DepartmentDto> {

    @Override
    public ValidationResult isValid(DepartmentDto dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            result.add("department", "Department data is missing");
            return result;
        }

        required(dto.name(), "name", result);
        required(dto.code(), "code", result);

        return result;
    }
}
package com.mayak.ietms.department.validator;

import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.user.validator.AbstractContractValidator;

public class DepartmentCreateContractValidator extends AbstractContractValidator implements Validator<DepartmentCreateDto> {

    @Override
    public ValidationResult isValid(DepartmentCreateDto dto) {
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
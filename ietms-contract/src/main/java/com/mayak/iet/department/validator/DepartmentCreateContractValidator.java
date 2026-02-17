package com.mayak.iet.department.validator;

import com.mayak.iet.department.dto.DepartmentCreateDto;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;
import com.mayak.iet.user.validator.AbstractContractValidator;

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
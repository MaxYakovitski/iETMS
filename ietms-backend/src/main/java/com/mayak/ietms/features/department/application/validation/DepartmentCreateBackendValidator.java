package com.mayak.ietms.features.department.application.validation;

import com.mayak.ietms.department.validator.DepartmentCreateContractValidator;
import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.features.department.infra.persistence.DepartmentRepository;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.user.validator.AbstractContractValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentCreateBackendValidator extends AbstractContractValidator implements Validator<DepartmentCreateDto> {

    private final DepartmentRepository departmentRepository;
    private final DepartmentCreateContractValidator contractValidator;

    @Override
    public ValidationResult isValid(DepartmentCreateDto object) {
        var result = contractValidator.isValid(object);

        if (!result.isValid()) {
            return result;
        }

        if (departmentRepository.existsByName(object.name())) {
            result.add("name", "Department with this name already exists");
        }

        if (departmentRepository.existsByCode(object.code())) {
            result.add("code", "Department with this code already exists");
        }

        return result;
    }
}
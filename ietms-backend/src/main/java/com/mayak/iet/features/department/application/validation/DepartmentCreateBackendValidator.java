package com.mayak.iet.features.department.application.validation;

import com.mayak.iet.department.validator.DepartmentCreateContractValidator;
import com.mayak.iet.department.dto.DepartmentCreateDto;
import com.mayak.iet.features.department.infra.persistence.DepartmentRepository;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;
import com.mayak.iet.user.validator.AbstractContractValidator;
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
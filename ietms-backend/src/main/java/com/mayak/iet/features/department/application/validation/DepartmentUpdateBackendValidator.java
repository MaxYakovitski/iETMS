package com.mayak.iet.features.department.application.validation;

import com.mayak.iet.department.validator.DepartmentUpdateContractValidator;
import com.mayak.iet.department.dto.DepartmentDto;
import com.mayak.iet.features.department.infra.persistence.DepartmentRepository;
import com.mayak.iet.common.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepartmentUpdateBackendValidator {

    private final DepartmentRepository departmentRepository;
    private final DepartmentUpdateContractValidator contractValidator;

    public ValidationResult isValid(Long id, DepartmentDto dto) {
        ValidationResult result = contractValidator.isValid(dto);

        if (!result.isValid()) return result;

        if (id == null || dto.id() == null || !id.equals(dto.id())) {
            result.add("id", "Invalid department id");
            return result;
        }

        if (!departmentRepository.existsById(id)) {
            result.add("id", "Department not found");
        }

        if (departmentRepository.existsByCodeAndIdNot(dto.code(), id)) {
            result.add("code", "Department code already exists");
        }

        return result;
    }
}

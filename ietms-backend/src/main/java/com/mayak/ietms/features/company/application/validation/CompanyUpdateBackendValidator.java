package com.mayak.ietms.features.company.application.validation;

import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.company.validator.CompanyUpdateContractValidator;
import com.mayak.ietms.features.company.infra.persistence.CompanyRepository;
import com.mayak.ietms.common.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyUpdateBackendValidator {

    private final CompanyRepository companyRepository;
    private final CompanyUpdateContractValidator contractValidator;

    public ValidationResult isValid(Long id, CompanyDto dto) {
        ValidationResult result = contractValidator.isValid(dto);

        if (!result.isValid()) {
            return result;
        }

        if (companyRepository.existsByNameAndIdNot(dto.name(), id)) {
            result.add("name", "Company with this name already exists");
        }

        return result;
    }
}

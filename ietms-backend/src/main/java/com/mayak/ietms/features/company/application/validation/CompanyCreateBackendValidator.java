package com.mayak.ietms.features.company.application.validation;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.validator.CompanyCreateContractValidator;
import com.mayak.ietms.features.company.infra.persistence.CompanyRepository;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompanyCreateBackendValidator implements Validator<CompanyCreateDto> {

    private final CompanyRepository companyRepository;
    private final CompanyCreateContractValidator contractValidator;

    @Override
    public ValidationResult isValid(CompanyCreateDto object) {
        var result = contractValidator.isValid(object);

        if (!result.isValid()) return result;

        if (companyRepository.existsByName(object.name())) {
            result.add("name", "Company with this name already exists");
        }

        return result;
    }
}
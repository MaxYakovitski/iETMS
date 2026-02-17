package com.mayak.iet.company.validator;

import com.mayak.iet.company.dto.CompanyCreateDto;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;
import com.mayak.iet.user.validator.AbstractContractValidator;

public class CompanyCreateContractValidator extends AbstractContractValidator implements Validator<CompanyCreateDto> {

    @Override
    public ValidationResult isValid(CompanyCreateDto object) {
        ValidationResult result = new ValidationResult();

        if (object == null) {
            result.add("customer", "Company data is missing");
            return result;
        }

        required(object.name(), "name", result);

        return result;
    }
}

package com.mayak.ietms.company.validator;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.user.validator.AbstractContractValidator;

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

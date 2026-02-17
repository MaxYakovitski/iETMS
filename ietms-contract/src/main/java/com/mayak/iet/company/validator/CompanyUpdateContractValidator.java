package com.mayak.iet.company.validator;

import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;
import com.mayak.iet.user.validator.AbstractContractValidator;

public class CompanyUpdateContractValidator extends AbstractContractValidator implements Validator<CompanyDto> {

    @Override
    public ValidationResult isValid(CompanyDto object) {
        ValidationResult result = new ValidationResult();

        if (object == null) {
            result.add("customer", "Company data is missing");
            return result;
        }

        required(object.name(), "name", result);

        return result;
    }
}

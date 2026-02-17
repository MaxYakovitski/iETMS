package com.mayak.ietms.company.validator;

import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.user.validator.AbstractContractValidator;

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

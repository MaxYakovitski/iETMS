package com.mayak.ietms.user.validator;

import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;

public class UserUpdateContractValidator extends AbstractUserContractValidator implements Validator<UserUpdateDto> {

    @Override
    public ValidationResult isValid(UserUpdateDto dto) {
        ValidationResult result = new ValidationResult();

        if (dto == null) {
            result.add("user", "User data is missing");

            return result;
        }

        required(dto.getName(), "name", result);
        required(dto.getSurname(), "surname", result);
        required(dto.getEmail(), "email", result);
        required(dto.getUserType(), "type", result);

        validateProfile(dto.getUserType(), dto.getDepartmentId(), dto.getRole(), dto.getPriority(), result);

        return result;
    }
}
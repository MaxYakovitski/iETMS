package com.mayak.ietms.user.validator;

import com.mayak.ietms.common.validation.ValidationUtils;
import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;

public class UserCreateContractValidator extends AbstractUserContractValidator implements Validator<UserCreateDto> {

    @Override
    public ValidationResult isValid(UserCreateDto dto) {
        ValidationResult result = new ValidationResult();
        if (dto == null) {
            result.add("user", "User data is missing");
            return result;
        }

        required(dto.getName(), "name", result);
        required(dto.getSurname(), "surname", result);
        required(dto.getEmail(), "email", result);
        if (dto.getEmail() != null && !ValidationUtils.isValidEmail(dto.getEmail())) {
            result.add("email", "Invalid email format!");
        }
        required(dto.getPassword(), "password", result);
        if (dto.getPassword() != null && !ValidationUtils.isValidPassword(dto.getPassword())) {
            result.add("password", "Password must be at least 8 characters and contain letters and numbers!");
        }
        required(dto.getUserType(), "type", result);

        validateProfile(
                dto.getUserType(),
                dto.getDepartmentId(),
                dto.getRole(),
                dto.getPriority(),
                result
        );

        return result;
    }
}
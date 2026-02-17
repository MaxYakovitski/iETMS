package com.mayak.iet.user.validator;

import com.mayak.iet.user.dto.UserCreateDto;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;

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
        required(dto.getPassword(), "password", result);
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
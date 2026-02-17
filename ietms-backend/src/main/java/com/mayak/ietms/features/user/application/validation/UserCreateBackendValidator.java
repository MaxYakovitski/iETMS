package com.mayak.ietms.features.user.application.validation;

import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.Validator;
import com.mayak.ietms.user.validator.AbstractUserContractValidator;
import com.mayak.ietms.user.validator.UserCreateContractValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCreateBackendValidator extends AbstractUserContractValidator implements Validator<UserCreateDto> {

    private final UserRepository userRepository;
    private final UserCreateContractValidator contractValidator;

    @Override
    public ValidationResult isValid(UserCreateDto dto) {
        ValidationResult result = contractValidator.isValid(dto);

        if (!result.isValid()) return result;

        if (userRepository.existsByEmail(dto.getEmail())) {
            result.add("email", "User with this email already exists");
        }

        if (dto.getUserType() == UserTypeDto.MANAGER
                && dto.getDepartmentId() != null
                && userRepository.existsByProfileDepartmentIdAndUserType(
                dto.getDepartmentId(),
                UserType.MANAGER
        )) {

            result.add(
                    "departmentId",
                    "Department already has a manager"
            );
        }

        return result;
    }
}
package com.mayak.iet.features.user.application.validation;

import com.mayak.iet.user.dto.UserCreateDto;
import com.mayak.iet.user.dto.enums.UserTypeDto;
import com.mayak.iet.features.user.domain.enums.UserType;
import com.mayak.iet.features.user.infra.persistence.UserRepository;
import com.mayak.iet.common.validation.ValidationResult;
import com.mayak.iet.common.validation.Validator;
import com.mayak.iet.user.validator.AbstractUserContractValidator;
import com.mayak.iet.user.validator.UserCreateContractValidator;
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
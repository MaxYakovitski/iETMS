package com.mayak.ietms.features.user.application.validation;

import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.user.validator.UserUpdateContractValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUpdateBackendValidator {

    private final UserRepository userRepository;
    private final UserUpdateContractValidator contractValidator;

    public ValidationResult isValid(Long userId, UserUpdateDto dto) {

        ValidationResult result = contractValidator.isValid(dto);
        if (!result.isValid()) return result;

        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), userId)) {
            result.add("email", "User with this email already exists");
        }

        if (dto.getUserType() == UserTypeDto.MANAGER
                && dto.getDepartmentId() != null
                && userRepository.existsByProfileDepartmentIdAndUserTypeAndIdNot(
                dto.getDepartmentId(),
                UserType.MANAGER,
                userId
        )) {

            result.add(
                    "departmentId",
                    "Department already has a manager"
            );
        }

        return result;
    }
}
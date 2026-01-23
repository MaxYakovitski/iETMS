package com.mayak.iet.user.validator;

import com.mayak.iet.user.dto.enums.PriorityDto;
import com.mayak.iet.user.dto.enums.RoleDto;
import com.mayak.iet.user.dto.enums.UserTypeDto;
import com.mayak.iet.common.validation.ValidationResult;

public abstract class AbstractUserContractValidator extends AbstractContractValidator {

    protected void validateProfile(
            UserTypeDto type,
            Long departmentId,
            RoleDto role,
            PriorityDto priority,
            ValidationResult result
    ){
        if (type == null) return;

        if (type == UserTypeDto.ADMIN) return;

        if (departmentId == null) {
            result.add("departmentId", "Department is required");
        }

        if (type == UserTypeDto.EMPLOYEE && role == null) {
            result.add("role", "Role is required for employee");
        }

        if (type == UserTypeDto.MANAGER && priority == null) {
            result.add("priority", "Priority is required for manager");
        }
    }
}
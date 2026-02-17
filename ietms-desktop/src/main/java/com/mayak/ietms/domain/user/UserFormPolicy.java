package com.mayak.ietms.domain.user;

import com.mayak.ietms.user.dto.enums.UserTypeDto;

public class UserFormPolicy {

    public UserFormState apply(UserTypeDto type) {
        return new UserFormState(
                roleEnabled(type),
                priorityEnabled(type)
        );
    }

    private boolean roleEnabled(UserTypeDto type) {
        return type == UserTypeDto.EMPLOYEE;
    }

    private boolean priorityEnabled(UserTypeDto type) {
        return type == UserTypeDto.MANAGER;
    }
}

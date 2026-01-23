package com.mayak.iet.domain.user;

import com.mayak.iet.user.dto.enums.UserTypeDto;

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

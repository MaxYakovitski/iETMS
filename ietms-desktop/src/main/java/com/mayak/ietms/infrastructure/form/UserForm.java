package com.mayak.ietms.infrastructure.form;

import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {

    private String name;
    private String surname;
    private String email;
    private String password;
    private UserTypeDto userType;
    private RoleDto role;
    private PriorityDto priority;
    private Long departmentId;

    public UserCreateDto toCreateDto() {
        return new UserCreateDto(
                name,
                surname,
                email,
                password,
                userType,
                departmentId,
                role,
                priority
        );
    }

    public UserUpdateDto toUpdateDto(Long id) {
        return UserUpdateDto.builder()
                .id(id)
                .name(name)
                .surname(surname)
                .email(email)
                .userType(userType)
                .departmentId(departmentId)
                .role(role)
                .priority(priority)
                .build();
    }

    public void fillFrom(UserResponseDto user) {
        this.name = user.name();
        this.surname = user.surname();
        this.email = user.email();
        this.password = null;
        this.userType = user.userType();

        if (user.profile() != null) {
            this.role = user.profile().role();
            this.priority = user.profile().priority();
            this.departmentId = user.profile().departmentId();
        } else {
            this.role = null;
            this.priority = null;
            this.departmentId = null;
        }
    }
}
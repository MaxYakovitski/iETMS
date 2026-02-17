package com.mayak.ietms.user.dto;

import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDto {
    String name;
    String surname;
    String email;
    String password;
    UserTypeDto userType;

    Long departmentId;
    RoleDto role;
    PriorityDto priority;
}
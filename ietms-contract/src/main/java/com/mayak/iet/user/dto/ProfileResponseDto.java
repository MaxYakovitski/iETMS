package com.mayak.iet.user.dto;

import com.mayak.iet.user.dto.enums.PriorityDto;
import com.mayak.iet.user.dto.enums.RoleDto;

public record ProfileResponseDto (
        Long departmentId,
        String departmentName,
        RoleDto role,
        PriorityDto priority) {
}
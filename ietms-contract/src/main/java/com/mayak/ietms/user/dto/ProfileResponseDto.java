package com.mayak.ietms.user.dto;

import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;

public record ProfileResponseDto (
        Long departmentId,
        String departmentName,
        RoleDto role,
        PriorityDto priority) {
}
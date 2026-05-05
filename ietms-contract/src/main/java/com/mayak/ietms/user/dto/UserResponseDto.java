package com.mayak.ietms.user.dto;

import com.mayak.ietms.user.dto.enums.UserStatusDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;

public record UserResponseDto (
        Long id,
        UserStatusDto status,
        String name,
        String surname,
        String email,
        UserTypeDto userType,
        ProfileResponseDto profile) {
}
package com.mayak.ietms.user.dto;

import com.mayak.ietms.user.dto.enums.UserTypeDto;

public record UserResponseDto (
        Long id,
        String name,
        String surname,
        String email,
        UserTypeDto userType,
        ProfileResponseDto profile) {
}
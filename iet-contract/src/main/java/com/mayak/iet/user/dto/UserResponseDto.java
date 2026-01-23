package com.mayak.iet.user.dto;

import com.mayak.iet.user.dto.enums.UserTypeDto;

public record UserResponseDto (
        Long id,
        String name,
        String surname,
        String email,
        UserTypeDto userType,
        ProfileResponseDto profile) {
}
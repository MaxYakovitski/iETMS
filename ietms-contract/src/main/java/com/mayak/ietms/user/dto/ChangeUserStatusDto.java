package com.mayak.ietms.user.dto;

import com.mayak.ietms.user.dto.enums.UserStatusDto;

/**
 * Request body for changing a user's lifecycle status.
 */
public record ChangeUserStatusDto(UserStatusDto status) { }
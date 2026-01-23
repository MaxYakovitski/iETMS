package com.mayak.iet.ui.core;

import com.mayak.iet.user.dto.UserResponseDto;

public interface SecuredView {
    void setLoggedInUser(UserResponseDto user);
}
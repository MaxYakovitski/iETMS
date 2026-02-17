package com.mayak.ietms.ui.core;

import com.mayak.ietms.user.dto.UserResponseDto;

public interface SecuredView {
    void setLoggedInUser(UserResponseDto user);
}
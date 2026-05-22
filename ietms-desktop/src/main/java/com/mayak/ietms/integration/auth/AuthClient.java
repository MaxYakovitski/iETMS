package com.mayak.ietms.integration.auth;

import com.mayak.ietms.integration.auth.dto.LoginResponseDto;

public interface AuthClient {
    LoginResponseDto login(String email, String password);
    LoginResponseDto refresh(String refreshToken);
    void logout(String refreshToken);
}
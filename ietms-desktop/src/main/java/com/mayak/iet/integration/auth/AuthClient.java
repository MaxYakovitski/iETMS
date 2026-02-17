package com.mayak.iet.integration.auth;

import com.mayak.iet.integration.auth.dto.LoginResponseDto;

public interface AuthClient {
    LoginResponseDto login(String email, String password);
}
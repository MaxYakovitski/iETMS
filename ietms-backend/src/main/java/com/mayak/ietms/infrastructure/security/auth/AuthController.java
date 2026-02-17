package com.mayak.ietms.infrastructure.security.auth;

import com.mayak.ietms.infrastructure.security.auth.dto.LoginRequest;
import com.mayak.ietms.infrastructure.security.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        String token = authService.login(
                request.email(),
                request.password()
        );

        return new LoginResponse(token);
    }
}
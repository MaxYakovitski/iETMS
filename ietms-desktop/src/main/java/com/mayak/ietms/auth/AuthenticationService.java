package com.mayak.ietms.auth;

import com.mayak.ietms.integration.auth.AuthClient;
import com.mayak.ietms.integration.auth.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author ma_yak
 */

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthClient authClient;

    public LoginResponseDto authenticate (String email, String password) {
        return authClient.login(email, password);
    }
}

package com.mayak.ietms.integration.bridge;

import com.mayak.ietms.integration.auth.AuthState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AuthState authState;

    public Optional<String> getToken() {
        return Optional.ofNullable(authState.getToken());
    }
}
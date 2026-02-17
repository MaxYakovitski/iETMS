package com.mayak.iet.integration.bridge;

import com.mayak.iet.integration.auth.AuthState;
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
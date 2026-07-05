package com.mayak.ietms.integration.auth;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.auth.dto.LoginRequestDto;
import com.mayak.ietms.integration.auth.dto.LoginResponseDto;
import com.mayak.ietms.integration.auth.dto.RefreshTokenRequestDto;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.rest.AbstractRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * REST implementation of {@link AuthClient}.
 * Handles login requests against the backend {@code /api/auth/login} endpoint.
 * A 401 response is treated as an {@link ApiException} rather than a session expiry,
 * since the user has not yet established a session.
 */
@Service
@Slf4j
public class AuthRestClient extends AbstractRestClient implements AuthClient {

    private static final String API = "/api/auth/login";

    public AuthRestClient(RestTemplate restTemplate, BackendConnectionMonitor monitor) {
        super(restTemplate, monitor);
    }

    @Override
    public LoginResponseDto login(String email, String password) {
        return exchangeSafely(() -> {
            try {
                return restTemplate.postForObject(
                        API,
                        new LoginRequestDto(email, password),
                        LoginResponseDto.class
                );
            } catch (HttpClientErrorException.Unauthorized e) {
                throw new ApiException(e.getStatusCode(), e.getResponseBodyAsString());
            }
        });
    }

    @Override
    public void logout(String refreshToken) {
        try {
            restTemplate.postForObject(
                    "/api/auth/logout",
                    new RefreshTokenRequestDto(refreshToken),
                    Void.class
            );
        } catch (Exception e) {
            log.warn("[auth] Logout request failed: {}", e.getMessage());
        }
    }
}

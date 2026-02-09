package com.mayak.iet.integration.auth;

import com.mayak.iet.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.iet.integration.auth.dto.LoginRequestDto;
import com.mayak.iet.integration.auth.dto.LoginResponseDto;
import com.mayak.iet.integration.rest.AbstractRestClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthRestClient extends AbstractRestClient implements AuthClient {

    private static final String API = "/api/auth/login";

    public AuthRestClient(RestTemplate restTemplate, BackendConnectionMonitor monitor) {
        super(restTemplate, monitor);
    }

    @Override
    public LoginResponseDto login(String email, String password) {
        return exchangeSafely(() -> restTemplate.postForObject(
                API,
                new LoginRequestDto(email, password),
                LoginResponseDto.class
        ));
    }
}
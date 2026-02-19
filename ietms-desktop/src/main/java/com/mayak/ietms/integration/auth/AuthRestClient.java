package com.mayak.ietms.integration.auth;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.auth.dto.LoginRequestDto;
import com.mayak.ietms.integration.auth.dto.LoginResponseDto;
import com.mayak.ietms.integration.rest.AbstractRestClient;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthRestClient extends AbstractRestClient implements AuthClient {

    private static final String API = "/api/auth/login";

    public AuthRestClient(RestTemplate restTemplate, BackendConnectionMonitor monitor,  SessionManager sessionManager) {
        super(restTemplate, monitor,  sessionManager);
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
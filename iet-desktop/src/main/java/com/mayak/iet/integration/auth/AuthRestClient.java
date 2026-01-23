package com.mayak.iet.integration.auth;

import com.mayak.iet.integration.auth.dto.LoginRequestDto;
import com.mayak.iet.integration.auth.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AuthRestClient implements AuthClient {

    private final RestTemplate restTemplate;

    @Value("${backend.base-url}")
    private String baseUrl;

    public LoginResponseDto login(String email, String password) {
        return restTemplate.postForObject(
                baseUrl + "/auth/login",
                new LoginRequestDto(email, password),
                LoginResponseDto.class
        );
    }
}
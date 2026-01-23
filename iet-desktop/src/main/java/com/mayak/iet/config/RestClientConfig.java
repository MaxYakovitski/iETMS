package com.mayak.iet.config;

import com.mayak.iet.integration.auth.AuthState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Value("${backend.base-url}")
    private String backendBaseUrl;

    @Bean
    public RestTemplate restTemplate(AuthState authState) {

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setUriTemplateHandler(
                new DefaultUriBuilderFactory(backendBaseUrl)
        );

        restTemplate.getInterceptors().add((request, body, execution) -> {
            String token = authState.getToken();
            if (token != null && !token.isBlank()) {
                request.getHeaders().setBearerAuth(token);
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
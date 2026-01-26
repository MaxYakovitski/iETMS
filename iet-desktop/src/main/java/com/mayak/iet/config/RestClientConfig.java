package com.mayak.iet.config;

import com.mayak.iet.app.BackendProperties;
import com.mayak.iet.integration.auth.AuthState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(AuthState authState, BackendProperties backendProperties) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(backendProperties.baseUrl()));

        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (authState.getToken() != null) {
                request.getHeaders().setBearerAuth(authState.getToken());
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
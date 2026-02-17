package com.mayak.ietms.config;

import com.mayak.ietms.app.BackendProperties;
import com.mayak.ietms.integration.auth.AuthState;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(AuthState authState, BackendProperties backendProperties) {

        CloseableHttpClient client = HttpClients.custom().disableAutomaticRetries().build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(backendProperties.getBaseUrl()));

        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (authState.getToken() != null) {
                request.getHeaders().setBearerAuth(authState.getToken());
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
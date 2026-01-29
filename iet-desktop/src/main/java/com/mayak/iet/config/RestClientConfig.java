package com.mayak.iet.config;

import com.mayak.iet.app.BackendProperties;
import com.mayak.iet.integration.auth.AuthState;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    @Qualifier("backendRestTemplate")
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

    @Bean
    @Qualifier("updateRestTemplate")
    public RestTemplate updateRestTemplate() {

        CloseableHttpClient client = HttpClients.custom()
                .disableAutomaticRetries()
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(client);

        return new RestTemplate(factory);
    }
}
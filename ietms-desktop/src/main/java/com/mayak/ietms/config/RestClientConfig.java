package com.mayak.ietms.config;

import com.mayak.ietms.app.BackendProperties;
import com.mayak.ietms.integration.auth.AuthState;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate(AuthState authState, BackendProperties backendProperties) {
        return buildRestTemplate(authState, backendProperties, 2, 2);
    }

    @Bean("reportRestTemplate")
    public RestTemplate reportRestTemplate(AuthState authState, BackendProperties backendProperties) {
        return buildRestTemplate(authState, backendProperties, 30, 10);
    }

    private RestTemplate buildRestTemplate(AuthState authState, BackendProperties backendProperties,
                                           int responseTimeoutSec, int connectionTimeoutSec) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(responseTimeoutSec))
                .setConnectionRequestTimeout(Timeout.ofSeconds(connectionTimeoutSec))
                .build();

        CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.ofSeconds(30))
                .build();

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
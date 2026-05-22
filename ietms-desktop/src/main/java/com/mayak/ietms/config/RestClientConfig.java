package com.mayak.ietms.config;

import com.mayak.ietms.app.BackendProperties;
import com.mayak.ietms.integration.auth.AuthState;
import com.mayak.ietms.integration.auth.dto.LoginResponseDto;
import com.mayak.ietms.integration.auth.dto.RefreshTokenRequestDto;
import com.mayak.ietms.integration.exception.SessionExpiredException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate(AuthState authState, BackendProperties backendProperties) {
        return buildRestTemplate(authState, backendProperties, 5, 2);
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

        RestTemplate noAuthTemplate = new RestTemplate(factory);
        noAuthTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(backendProperties.getBaseUrl()));

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(backendProperties.getBaseUrl()));
        restTemplate.getInterceptors().add((request, body, execution) -> {
            if (authState.getToken() != null) {
                request.getHeaders().setBearerAuth(authState.getToken());
            }

            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() == 401
                    && authState.getRefreshToken() != null) {
                response.close();

                String tokenBeforeRefresh = authState.getToken();
                synchronized (authState) {
                    if (!tokenBeforeRefresh.equals(authState.getToken())) {
                        HttpRequest retryRequest = buildRetryRequest(request, authState.getToken());
                        return execution.execute(retryRequest, body);
                    }
                    try {
                        LoginResponseDto refreshed = noAuthTemplate.postForObject(
                                "/api/auth/refresh",
                                new RefreshTokenRequestDto(authState.getRefreshToken()),
                                LoginResponseDto.class
                        );
                        if (refreshed != null) {
                            authState.setToken(refreshed.accessToken());
                            authState.setRefreshToken(refreshed.refreshToken());
                            HttpRequest retryRequest = buildRetryRequest(request, refreshed.accessToken());
                            return execution.execute(retryRequest, body);
                        }
                    } catch (Exception e) {
                        throw new SessionExpiredException();
                    }
                }
            }
            return response;
        });
        return restTemplate;
    }

    private HttpRequest buildRetryRequest(HttpRequest original, String newToken) {
        return new HttpRequestWrapper(original) {
            @Override
            public @NotNull HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.setBearerAuth(newToken);
                return headers;
            }
        };
    }
}
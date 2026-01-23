package com.mayak.iet.integration.rest;

import com.mayak.iet.integration.exception.ApiException;
import com.mayak.iet.integration.exception.ApiValidationException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

public class AbstractRestClient {
    protected final RestTemplate restTemplate;

    protected AbstractRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> T exchangeSafely(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (HttpStatusCodeException ex) {
            throw mapHttpException(ex);
        } catch (RestClientException ex) {
            throw new ApiException("Remote service unavailable", ex);
        } catch (Exception ex) {
            throw new ApiException("Unexpected client error", ex);
        }
    }

    protected RuntimeException mapHttpException(HttpStatusCodeException ex) {

        int status = ex.getStatusCode().value();
        String body = ex.getResponseBodyAsString();

        if (status == 400 && body.contains("\"errors\"")) {
            return ApiValidationException.fromResponse(ex);
        }

        if (status == 401 || status == 403) return new ApiException(ex.getStatusCode(), "Access denied");
        if (status == 404) return new ApiException(ex.getStatusCode(), "Resource not found");
        return new ApiException(ex.getStatusCode(), body);
    }
}
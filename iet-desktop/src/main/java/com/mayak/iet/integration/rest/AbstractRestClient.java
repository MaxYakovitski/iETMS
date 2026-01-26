package com.mayak.iet.integration.rest;

import com.mayak.iet.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.iet.integration.exception.ApiException;
import com.mayak.iet.integration.exception.ApiValidationException;
import lombok.AllArgsConstructor;
import org.springframework.web.client.*;

import java.util.function.Supplier;

@AllArgsConstructor
public class AbstractRestClient {

    protected final RestTemplate restTemplate;
    protected final BackendConnectionMonitor connectionMonitor;

    protected <T> T exchangeSafely(Supplier<T> supplier) {
        return execute(() -> {
        try {
            return supplier.get();
        } catch (HttpStatusCodeException ex) {
            throw mapHttpException(ex);
        } catch (RestClientException ex) {
            throw new ApiException("Remote service unavailable", ex);
        } catch (Exception ex) {
            throw new ApiException("Unexpected client error", ex);
        }
        });
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

    protected <T> T execute(Supplier<T> call) {
        try {
            T result = call.get();
            connectionMonitor.markConnected();
            return result;
        } catch (HttpStatusCodeException e) {
            connectionMonitor.markConnected();
            throw e;
        } catch (ResourceAccessException e) {
            connectionMonitor.markDisconnected(e);
            throw e;
        }
    }
}
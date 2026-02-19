package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.ApiValidationException;
import com.mayak.ietms.integration.exception.SessionExpiredException;
import com.mayak.ietms.ui.core.SessionManager;
import lombok.AllArgsConstructor;
import org.springframework.web.client.*;

import java.util.function.Supplier;

@AllArgsConstructor
public class AbstractRestClient {

    protected final RestTemplate restTemplate;
    protected final BackendConnectionMonitor connectionMonitor;
    protected final SessionManager sessionManager;

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

        if (status == 401) {
            return new SessionExpiredException();
        }

        if (status == 400 && body.contains("\"errors\"")) {
            return ApiValidationException.fromResponse(ex);
        }

        return new ApiException(ex.getStatusCode(), body);
    }

    protected <T> T execute(Supplier<T> call) {
        try {
            T result = call.get();
            connectionMonitor.markConnected();
            return result;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == 401) {
                sessionManager.handleSessionExpired();
            }

            connectionMonitor.markConnected();
            throw e;
        } catch (ResourceAccessException e) {
            connectionMonitor.markDisconnected(e);
            throw e;
        }
    }
}
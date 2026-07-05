package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.ApiValidationException;
import com.mayak.ietms.integration.exception.NetworkUnavailableException;
import com.mayak.ietms.integration.exception.SessionExpiredException;
import lombok.AllArgsConstructor;
import org.springframework.web.client.*;

import java.util.function.Supplier;

@AllArgsConstructor
public class AbstractRestClient {

    protected final RestTemplate restTemplate;
    protected final BackendConnectionMonitor connectionMonitor;

    protected <T> T exchangeSafely(Supplier<T> supplier) {
        try {
            T result = supplier.get();
            connectionMonitor.markConnected();
            return result;
        } catch (HttpStatusCodeException ex) {
            connectionMonitor.markConnected();
            throw mapHttpException(ex);
        } catch (ResourceAccessException ex) {
            connectionMonitor.markDisconnected(ex);
            throw new NetworkUnavailableException(ex);
        }
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
}

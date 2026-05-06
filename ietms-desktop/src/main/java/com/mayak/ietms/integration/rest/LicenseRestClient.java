package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.api.LicenseClient;
import com.mayak.ietms.license.dto.LicenseActivateDto;
import com.mayak.ietms.license.dto.LicenseResponseDto;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * REST implementation of {@link LicenseClient}.
 * Communicates with the backend {@code /api/license} endpoint.
 */
@Service
public class LicenseRestClient extends AbstractRestClient implements LicenseClient {

    private static final String API = "/api/license";

    public LicenseRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor, SessionManager sessionManager) {
        super(restTemplate, connectionMonitor, sessionManager);
    }

    @Override
    public Optional<LicenseResponseDto> getCurrent() {
        return exchangeSafely(() -> {
            try {
                ResponseEntity<LicenseResponseDto> response = restTemplate.exchange(
                        API,
                        HttpMethod.GET,
                        null,
                        LicenseResponseDto.class
                );
                return Optional.ofNullable(response.getBody());
            } catch (HttpClientErrorException.NotFound e) {
                return Optional.empty();
            }
        });
    }

    @Override
    public void activate(LicenseActivateDto dto) {
        exchangeSafely(() -> {
            RequestEntity<LicenseActivateDto> request = RequestEntity
                    .post(API)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void deactivate() {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(API)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}
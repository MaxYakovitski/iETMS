package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.location.dto.LocationCreateDto;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.integration.api.LocationClient;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class LocationRestClient extends AbstractRestClient implements LocationClient {

    private static final String API = "/api/locations";

    public LocationRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor, SessionManager sessionManager) {
        super(restTemplate, connectionMonitor, sessionManager);
    }

    @Override
    public List<LocationDto> findAll() {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API)
                    .build();

            var body = restTemplate.exchange(
                            request,
                            new ParameterizedTypeReference<List<LocationDto>>() {}
                    ).getBody();

            return body != null ? body : List.of();
        });
    }

    @Override
    public void create(LocationCreateDto dto) {
        exchangeSafely(() -> {
            RequestEntity<LocationCreateDto> request = RequestEntity
                    .post(API)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void update(LocationDto dto) {
        exchangeSafely(() -> {
            RequestEntity<LocationDto> request = RequestEntity
                    .put(API)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void delete(Long id) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(API + "/{id}", id)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}
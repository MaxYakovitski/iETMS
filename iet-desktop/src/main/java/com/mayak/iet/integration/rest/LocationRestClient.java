package com.mayak.iet.integration.rest;

import com.mayak.iet.location.dto.LocationCreateDto;
import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.integration.api.LocationClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class LocationRestClient extends AbstractRestClient implements LocationClient {

    private static final String API = "/api/locations";

    public LocationRestClient(RestTemplate restTemplate) {
        super(restTemplate);
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
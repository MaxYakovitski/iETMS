package com.mayak.iet.integration.rest;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneViewDto;
import com.mayak.iet.integration.api.LaneClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class LaneRestClient extends AbstractRestClient implements LaneClient {

    private static final String API = "/api/lanes";

    public LaneRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<LaneViewDto> findByCompany(long companyId) {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API + "/by-company/{companyId}", companyId)
                    .build();

            var body = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<LaneViewDto>>() {}
            ).getBody();

            return body != null ? body : List.of();
        });
    }

    @Override
    public LaneViewDto create(long companyId, LaneCreateDto dto) {
        return exchangeSafely(() -> {
            RequestEntity<LaneCreateDto> request = RequestEntity
                    .post(API + "/by-company/{companyId}", companyId)
                    .body(dto);

            return restTemplate.exchange(
                    request,
                    LaneViewDto.class
            ).getBody();
        });
    }

    @Override
    public LaneViewDto update(long laneId, LaneCreateDto dto) {
        return exchangeSafely(() -> {
            RequestEntity<LaneCreateDto> request = RequestEntity
                    .put(API + "/{id}", laneId)
                    .body(dto);

            return restTemplate.exchange(
                    request,
                    LaneViewDto.class
            ).getBody();
        });
    }

    @Override
    public void delete(long laneId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(API + "/{id}", laneId)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}
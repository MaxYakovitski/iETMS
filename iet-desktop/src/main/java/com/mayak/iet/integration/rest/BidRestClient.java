package com.mayak.iet.integration.rest;

import com.mayak.iet.request.dto.bid.BidCreateDto;
import com.mayak.iet.request.dto.bid.BidViewDto;
import com.mayak.iet.integration.api.BidClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class BidRestClient extends AbstractRestClient implements BidClient {

    private static final String BASE_URL = "http://localhost:8080/api/bids";

    public BidRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<BidViewDto> findByRequest(long requestId) {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(BASE_URL + "/by-request/{id}", requestId)
                    .build();

            var response = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<BidViewDto>>() {}
            );

            return response.getBody() != null
                    ? response.getBody()
                    : Collections.emptyList();
        });
    }

    @Override
    public BidViewDto create(BidCreateDto dto) {
        return exchangeSafely(() -> {
            RequestEntity<BidCreateDto> request = RequestEntity
                    .post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            return restTemplate
                    .exchange(request, BidViewDto.class)
                    .getBody();
        });
    }

    @Override
    public void delete(long bidId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(BASE_URL + "/{id}", bidId)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}
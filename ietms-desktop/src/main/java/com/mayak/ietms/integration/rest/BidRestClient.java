package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.integration.api.BidClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class BidRestClient extends AbstractRestClient implements BidClient {

    private static final String API = "/api/bids";

    public BidRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor) {
        super(restTemplate,  connectionMonitor);
    }

    @Override
    public List<BidViewDto> findByRequest(long requestId) {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API + "/by-request/{id}", requestId)
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
                    .post(API)
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
                    .delete(API + "/{id}", bidId)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}
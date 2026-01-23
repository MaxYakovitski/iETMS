package com.mayak.iet.integration.rest;

import com.mayak.iet.common.dto.page.PageDto;
import com.mayak.iet.request.dto.command.AcceptRequest;
import com.mayak.iet.request.dto.command.RefuseRequest;
import com.mayak.iet.request.dto.command.UpdateTidRequest;
import com.mayak.iet.request.dto.create.BaseRequestDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.filter.RequestFilterDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.request.dto.view.RequestListItemDto;
import com.mayak.iet.integration.api.RequestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class RequestRestClient extends AbstractRestClient implements RequestClient {

    private static final String BASE_URL = "http://localhost:8080/api/requests";

    public RequestRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public PageDto<RequestListItemDto> findPage(int page, int size, RequestTypeDto type) {
        return exchangeSafely(() -> {

            String url = BASE_URL + "?page={page}&size={size}&type={type}";

            RequestEntity<Void> request = RequestEntity
                    .get(url, page, size, type)
                    .build();

            return restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<PageDto<RequestListItemDto>>() {}
            ).getBody();
        });
    }

    @Override
    public PageDto<RequestListItemDto> search(String query, int page, int size) {
        return exchangeSafely(() -> {

            String url = BASE_URL + "/search?q={q}&page={page}&size={size}";

            RequestEntity<Void> request = RequestEntity
                    .get(url, query, page, size)
                    .build();

            return restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<PageDto<RequestListItemDto>>() {}
            ).getBody();
        });
    }

    @Override
    public PageDto<RequestListItemDto> filter(RequestFilterDto filter, int page, int size) {
        return exchangeSafely(() -> {

            String url = BASE_URL + "/filter?page={page}&size={size}";

            RequestEntity<RequestFilterDto> request = RequestEntity
                    .post(url, page, size)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(filter);

            return restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<PageDto<RequestListItemDto>>() {}
            ).getBody();
        });
    }

    @Override
    public RequestDetailsDto getDetails(long requestId) {
        return exchangeSafely(() -> {

            RequestEntity<Void> request = RequestEntity
                    .get(BASE_URL + "/{id}", requestId)
                    .build();

            return restTemplate.exchange(request, RequestDetailsDto.class).getBody();
        });
    }

    @Override
    public void join(long requestId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .post(BASE_URL + "/" + requestId + "/join")
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void leave(long requestId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .post(BASE_URL + "/" + requestId + "/leave")
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void offer(long requestId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .post(BASE_URL + "/" + requestId + "/offer")
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void accept(long requestId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .post(BASE_URL + "/" + requestId + "/accept")
                    .contentType(MediaType.APPLICATION_JSON)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });

    }

    @Override
    public void accept(long requestId, BigDecimal price) {
        exchangeSafely(() -> {
            AcceptRequest body = new AcceptRequest(price);

            RequestEntity<AcceptRequest> request = RequestEntity
                    .post(BASE_URL + "/" + requestId + "/accept")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void refuse(long requestId, String reasonCode) {
        exchangeSafely(() -> {
            RefuseRequest body = new RefuseRequest(reasonCode);

            RequestEntity<RefuseRequest> request = RequestEntity
                    .post(BASE_URL + "/" + requestId + "/refuse")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public String getExchangeString(long requestId) {
        return exchangeSafely(() -> {

            RequestEntity<Void> request = RequestEntity
                    .get(BASE_URL + "/" + requestId + "/exchange")
                    .build();

            return restTemplate.exchange(request, String.class).getBody();
        });
    }

    @Override
    public void updateTid(long requestId, String tid) {
        exchangeSafely(() -> {

            UpdateTidRequest body = new UpdateTidRequest(tid);

            RequestEntity<UpdateTidRequest> request = RequestEntity
                    .patch(BASE_URL + "/" + requestId + "/tid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public RequestDetailsDto create(BaseRequestDto dto) {
        return exchangeSafely(() -> {

            RequestEntity<BaseRequestDto> request = RequestEntity
                    .post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            return restTemplate.exchange(request, RequestDetailsDto.class).getBody();
        });
    }

    @Override
    public void delete(long requestId) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(BASE_URL + "/" + requestId)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public boolean hasShipment(long requestId) {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(BASE_URL + "/{id}/has-shipment", requestId)
                    .build();

            Boolean result = restTemplate.exchange(
                    request,
                    Boolean.class
            ).getBody();

            return Boolean.TRUE.equals(result);
        });
    }
}
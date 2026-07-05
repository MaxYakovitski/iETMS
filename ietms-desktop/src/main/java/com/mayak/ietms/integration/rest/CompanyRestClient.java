package com.mayak.ietms.integration.rest;

import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.api.CompanyClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyRestClient extends AbstractRestClient implements CompanyClient {

    private static final String API = "/api/companies";

    public CompanyRestClient(RestTemplate restTemplate,  BackendConnectionMonitor connectionMonitor) {
        super(restTemplate,  connectionMonitor);
    }

    @Override
    public List<CompanyDto> findAll() {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API)
                    .build();
            var body = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<CompanyDto>>() {}
            ).getBody();
            return body != null ? body : List.of();
        });
    }

    @Override
    public Optional<CompanyDto> findByName(String name) {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API + "/by-name?name={name}", name)
                    .build();
            return Optional.ofNullable(restTemplate.exchange(request, CompanyDto.class).getBody());
        });
    }

    @Override
    public CompanyDto create(CompanyCreateDto dto) {
        return exchangeSafely(() -> {
            RequestEntity<CompanyCreateDto> request = RequestEntity
                    .post(API)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);
            return restTemplate.exchange(request, CompanyDto.class).getBody();
        });
    }

    @Override
    public void update(Long id, CompanyDto dto) {
        exchangeSafely(() -> {
            RequestEntity<CompanyDto> request = RequestEntity
                    .put(API + "/" + id)
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
                    .delete(API + "/" + id)
                    .build();
            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}

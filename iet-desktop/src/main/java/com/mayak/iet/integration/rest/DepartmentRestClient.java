package com.mayak.iet.integration.rest;

import com.mayak.iet.department.dto.DepartmentCreateDto;
import com.mayak.iet.department.dto.DepartmentDto;
import com.mayak.iet.integration.api.DepartmentClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DepartmentRestClient extends AbstractRestClient implements DepartmentClient {

    private static final String BASE_URL = "http://localhost:8080/api/departments";

    public DepartmentRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<DepartmentDto> findAll() {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(BASE_URL)
                    .build();

            var body = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<List<DepartmentDto>>() {}
            ).getBody();

            return body != null ? body : List.of();
        });
    }

    @Override
    public void create(DepartmentCreateDto dto) {
        exchangeSafely(() -> {
            RequestEntity<DepartmentCreateDto> request = RequestEntity
                    .post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void update(DepartmentDto dto) {
        exchangeSafely(() -> {
            RequestEntity<DepartmentDto> request = RequestEntity
                    .put(BASE_URL + "/{id}", dto.id())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto);

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }

    @Override
    public void delete(long id) {
        exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .delete(BASE_URL + "/{id}", id)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}

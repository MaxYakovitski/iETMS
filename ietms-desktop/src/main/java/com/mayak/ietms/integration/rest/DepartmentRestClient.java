package com.mayak.ietms.integration.rest;

import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.api.DepartmentClient;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DepartmentRestClient extends AbstractRestClient implements DepartmentClient {

    private static final String API = "/api/departments";

    public DepartmentRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor, SessionManager sessionManager) {
        super(restTemplate,   connectionMonitor, sessionManager);
    }

    @Override
    public List<DepartmentDto> findAll() {
        return exchangeSafely(() -> {
            RequestEntity<Void> request = RequestEntity
                    .get(API)
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
                    .post(API)
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
                    .put(API + "/{id}", dto.id())
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
                    .delete(API + "/{id}", id)
                    .build();

            restTemplate.exchange(request, Void.class);
            return null;
        });
    }
}

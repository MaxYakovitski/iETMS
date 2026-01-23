package com.mayak.iet.features.request.infra.persistence;

import com.mayak.iet.request.dto.filter.RequestFilterDto;
import com.mayak.iet.features.request.domain.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepositoryCustom {
    Page<Request> filterByQuery(RequestFilterDto filter, Pageable pageable);
    Page<Request> searchByQuery(String query,Pageable pageable);
}
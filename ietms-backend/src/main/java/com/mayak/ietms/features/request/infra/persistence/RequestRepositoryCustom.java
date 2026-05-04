package com.mayak.ietms.features.request.infra.persistence;

import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.features.request.domain.model.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RequestRepositoryCustom {
    Page<Request> filterByQuery(RequestFilterDto filter, Long departmentId, Pageable pageable);
    Page<Request> searchByQuery(String query, RequestTypeDto type, Long departmentId, Pageable pageable);
}
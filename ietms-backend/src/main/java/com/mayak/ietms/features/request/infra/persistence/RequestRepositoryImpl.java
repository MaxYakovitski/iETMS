package com.mayak.ietms.features.request.infra.persistence;

import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.features.request.domain.model.Request;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class RequestRepositoryImpl implements RequestRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Request> filterByQuery(RequestFilterDto filter, Long departmentId, Pageable pageable) {
        if (pageable == null) pageable = Pageable.unpaged();

        RequestSqlBuilder builder = new RequestSqlBuilder()
                .applyDepartmentFilter(departmentId)
                .applyFromLocationFilter(filter.getFromCountry(), filter.getFromZipCode(), filter.getFromPlace())
                .applyToLocationFilter(filter.getToCountry(), filter.getToZipCode(), filter.getToPlace())
                .applyStatusFilter(filter.getStatuses())
                .applyRequestTypeFilter(filter.getRequestTypes())
                .applyShipmentTypeFilter(filter.getShipmentTypes())
                .applyTransportTypeFilter(filter.getTransportTypes())
                .applyCompanyFilter(filter.getCompanyId(), filter.getCompanyName())
                .applyDateFilter(filter.getDatesFilterOption(), filter.getStartDate(), filter.getEndDate())
                .applyDangerousFilter(filter.getDangerous())
                .applyWeightFilter(filter.getMinWeight(), filter.getMaxWeight())
                .applyLdmFilter(filter.getMinLdm(), filter.getMaxLdm())
                .applyAuthorFilter(filter.getAuthorIds())
                .applyCompetitorFilter(filter.getCompetitorIds())
                .applyDispatcherFilter(filter.getDispatchersIds());

        String sql = builder.toSqlFragment();
        Map<String, Object> params = builder.getParams();

        long total = executeCount(sql, params);
        List<Request> content = executeSelect(sql, params, pageable);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Request> searchByQuery(String query, RequestTypeDto type, Long departmentId, Pageable pageable) {
        if (pageable == null) pageable = Pageable.unpaged();
        if (query == null || query.isBlank()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        RequestSqlBuilder builder = new RequestSqlBuilder()
                .applyTypeFilter(type)
                .applyDepartmentFilter(departmentId)
                .applyTextSearch(query);

        String sql = builder.toSqlFragment();
        Map<String, Object> params = builder.getParams();

        long total = executeCount(sql, params);
        List<Request> content = executeSelect(sql, params, pageable);

        log.info("Search applied: '{}', found {} results (total {})", query, content.size(), total);
        return new PageImpl<>(content, pageable, total);
    }

    // ─────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────

    private long executeCount(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery("SELECT COUNT(*) " + sql);
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    private List<Request> executeSelect(String sql, Map<String, Object> params, Pageable pageable) {
        Query query = entityManager.createNativeQuery(
                "SELECT r.* " + sql + RequestSqlBuilder.ORDER_BY, Request.class);
        params.forEach(query::setParameter);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }
}
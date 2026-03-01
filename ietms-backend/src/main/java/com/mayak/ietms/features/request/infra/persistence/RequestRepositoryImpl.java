package com.mayak.ietms.features.request.infra.persistence;

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

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class RequestRepositoryImpl implements RequestRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final String orderBy = """
ORDER BY
    CASE r.status
        WHEN 'NEW' THEN 1
        WHEN 'IN_PROGRESS' THEN 2
        WHEN 'BIDDING' THEN 3
        WHEN 'OFFERED' THEN 4
        WHEN 'ACCEPTED' THEN 5
        WHEN 'REFUSED' THEN 6
        ELSE 7
    END,
    r.issue_date DESC
""";

    @Override
    @SuppressWarnings("unchecked")
    public Page<Request> filterByQuery(RequestFilterDto filter, Pageable pageable) {
        if (pageable == null) pageable = Pageable.unpaged();

        StringBuilder sql = new StringBuilder("""
        FROM requests r
        LEFT JOIN users a ON r.author_id = a.id
        LEFT JOIN company c ON r.customer_company_id = c.id
        WHERE r.archived = false
    """);

        Map<String, Object> params = new HashMap<>();

        // ISO / ZIP / PlaceName для from_order
        if (filter.getToCountry() != null || filter.getToZipCode() != null || filter.getToPlace() != null) {
            sql.append(" AND EXISTS (")
                    .append("SELECT 1 FROM jsonb_array_elements_text(r.to_location_ids_order) AS t(id) ")
                    .append("JOIN location l ON l.id = t.id::bigint WHERE 1=1");

            if (filter.getToCountry() != null) {
                sql.append(" AND l.country_code = :toIso");
                params.put("toIso", filter.getToCountry().toUpperCase());
            }
            if (filter.getToZipCode() != null) {
                sql.append(" AND l.zip_code LIKE :toZip");
                params.put("toZip", filter.getToZipCode() + "%");
            }
            if (filter.getToPlace() != null) {
                sql.append(" AND COALESCE(l.place_name,'') ILIKE :toPlace");
                params.put("toPlace", "%" + filter.getToPlace() + "%");
            }
            sql.append(")");
        }

        // ISO / ZIP / PlaceName для to_order
            sql.append(" AND (1=1"); // пачынаем OR блок
            if (filter.getToCountry() != null) {
                sql.append(" AND to_loc.country_code = :toIso");
                params.put("toIso", filter.getToCountry().toUpperCase());
            }
            if (filter.getToZipCode() != null) {
                sql.append(" AND to_loc.zip_code LIKE :toZip");
                params.put("toZip", filter.getToZipCode().toUpperCase() + "%");
            }
            if (filter.getToPlace() != null) {
                sql.append(" AND COALESCE(to_loc.place_name, '') LIKE :toPlace");
                params.put("toPlace", "%" + filter.getToPlace().toUpperCase() + "%");
            }
            sql.append(")");

        // Enum Lists
        if (!filter.getStatuses().isEmpty()) {
            sql.append(" AND r.status IN :statuses");
            params.put("statuses", filter.getStatuses().stream().map(Enum::name).toList());
        }
        if (!filter.getRequestTypes().isEmpty()) {
            sql.append(" AND r.request_type IN :types");
            params.put("types", filter.getRequestTypes());
        }
        if (!filter.getShipmentTypes().isEmpty()) {
            sql.append(" AND r.shipment_type IN :shipmentTypes");
            params.put("shipmentTypes", filter.getShipmentTypes().stream().map(Enum::name).toList());
        }
        if (!filter.getTransportTypes().isEmpty()) {
            sql.append(" AND r.transport_type IN :transportTypes");
            params.put("transportTypes", filter.getTransportTypes().stream().map(Enum::name).toList());
        }

        // Company
        if (filter.getCompanyId() != null) {
            sql.append(" AND r.customer_company_id = :companyId");
            params.put("companyId", filter.getCompanyId());
        }
        else if (filter.getCompanyName() != null && !filter.getCompanyName().isBlank()) {
            sql.append(" AND LOWER(c.name) LIKE :companyName");
            params.put("companyName", "%" + filter.getCompanyName().toLowerCase() + "%");
        }

        // Dates
        LocalDate startLocalDate = filter.getStartDate();
        LocalDate endLocalDate = filter.getEndDate();
        if (startLocalDate != null && endLocalDate == null) {
            endLocalDate = startLocalDate;
        }
        if (filter.getDatesFilterOption() != null && (startLocalDate != null || endLocalDate != null)) {
            final String column = switch (filter.getDatesFilterOption()) {
                case REQUEST_ISSUE_DATE -> "issue_date";
                case LOADING_DATE -> "start_date";
                case DELIVERY_DATE -> "end_date";
            };
            if (startLocalDate != null) {
                sql.append(" AND r.").append(column).append("::date >= :startDate");
                params.put("startDate", startLocalDate);
            }
            if (endLocalDate != null) {
                sql.append(" AND r.").append(column).append("::date <= :endDate");
                params.put("endDate", endLocalDate);
            }
        }

        // Dangerous
        if (filter.getDangerous() != null) {
            switch (filter.getDangerous()) {
                case ADR -> sql.append(" AND r.dangerous = true");
                case NON_ADR -> sql.append(" AND r.dangerous = false");
            }
        }

        // Weight & LDM
        if (filter.getMinWeight() != null) { sql.append(" AND r.weight >= :minWeight"); params.put("minWeight", filter.getMinWeight()); }
        if (filter.getMaxWeight() != null) { sql.append(" AND r.weight <= :maxWeight"); params.put("maxWeight", filter.getMaxWeight()); }
        if (filter.getMinLdm() != null) { sql.append(" AND r.loading_meter >= :minLdm"); params.put("minLdm", filter.getMinLdm()); }
        if (filter.getMaxLdm() != null) { sql.append(" AND r.loading_meter <= :maxLdm"); params.put("maxLdm", filter.getMaxLdm()); }

        // Authors
        if (filter.getAuthorIds() != null && !filter.getAuthorIds().isEmpty()) {
            sql.append(" AND r.author_id IN :authorIds");
            params.put("authorIds", filter.getAuthorIds());
        }

        // Competitors
        if (filter.getCompetitorIds() != null && !filter.getCompetitorIds().isEmpty()) {
            sql.append("""
        AND r.id IN (
            SELECT rc.request_id
            FROM request_competitors rc
            WHERE rc.user_id IN :competitorIds
        )
    """);
            params.put("competitorIds", filter.getCompetitorIds());
        }

        // Dispatchers
        if (filter.getDispatchersIds() != null && !filter.getDispatchersIds().isEmpty()) {
            sql.append("""
        AND r.dispatcher_id IN :dispatchersIds
    """);
            params.put("dispatchersIds", filter.getDispatchersIds());
        }

        // --- Count query ---
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + sql);
        params.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // --- Select query ---
        Query selectQuery = entityManager.createNativeQuery("SELECT r.* " + sql + " " + orderBy, Request.class);
        params.forEach(selectQuery::setParameter);

        selectQuery.setFirstResult((int) pageable.getOffset());
        selectQuery.setMaxResults(pageable.getPageSize());

        List<Request> content = selectQuery.getResultList();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Request> searchByQuery(String query, Pageable pageable) {
        if (pageable == null) pageable = Pageable.unpaged();
        if (query == null || query.isBlank()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        String likeQuery = "%" + query.toLowerCase() + "%";

        StringBuilder sql = new StringBuilder("""
        FROM requests r
        LEFT JOIN users a ON r.author_id = a.id
        LEFT JOIN company c ON r.customer_company_id = c.id
        WHERE r.archived = false
        AND (
            LOWER(COALESCE(r.customer_reference, '')) LIKE :query
            OR LOWER(COALESCE(r.tid, '')) LIKE :query
            OR LOWER(COALESCE(a.name, '') || ' ' || COALESCE(a.surname, '')) LIKE :query
            OR LOWER(COALESCE(c.name, '')) LIKE :query
            OR LOWER(COALESCE(r.comments, '')) LIKE :query
            OR LOWER(COALESCE(r.temperature, '')) LIKE :query
            OR EXISTS (
                       SELECT 1 FROM jsonb_array_elements_text(r.from_location_ids_order) AS f(id)
                       JOIN location l ON l.id = f.id::bigint
                       WHERE LOWER(COALESCE(l.zip_code::text, '')) LIKE :query
                       OR LOWER(COALESCE(l.place_name, '')) LIKE :query
            )
            OR EXISTS (
                       SELECT 1 FROM jsonb_array_elements_text(r.to_location_ids_order) AS t(id)
                       JOIN location l ON l.id = t.id::bigint
                       WHERE LOWER(COALESCE(l.zip_code::text, '')) LIKE :query
                       OR LOWER(COALESCE(l.place_name, '')) LIKE :query
            )
            OR CAST(r.id AS TEXT) LIKE :query
        )
    """);

        Map<String, Object> params = new HashMap<>();
        params.put("query", likeQuery);

        // --- Count query ---
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) " + sql);
        setQueryParameters(countQuery, params);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        // --- Select query ---
        Query selectQuery = entityManager.createNativeQuery("SELECT r.* " + sql + " " + orderBy, Request.class);
        setQueryParameters(selectQuery, params);

        selectQuery.setFirstResult((int) pageable.getOffset());
        selectQuery.setMaxResults(pageable.getPageSize());

        List<Request> content = selectQuery.getResultList();

        log.info("Search applied: '{}', found {} results (total {})", query, content.size(), total);

        return new PageImpl<>(content, pageable, total);
    }

    private void setQueryParameters(Query query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }
}
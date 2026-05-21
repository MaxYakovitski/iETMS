package com.mayak.ietms.features.request.infra.persistence;

import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.filter.DangerousFilterOption;
import com.mayak.ietms.request.dto.filter.DatesFilterOption;

import java.time.LocalDate;
import java.util.*;

public class RequestSqlBuilder {

    static final String BASE_FROM = """
            FROM requests r
            LEFT JOIN users a ON r.author_id = a.id
            LEFT JOIN company c ON r.customer_company_id = c.id
            WHERE r.archived = false
            """;

    static final String ORDER_BY = """
            ORDER BY
                CASE r.status
                    WHEN 'NEW'         THEN 1
                    WHEN 'IN_PROGRESS' THEN 2
                    WHEN 'BIDDING'     THEN 3
                    WHEN 'OFFERED'     THEN 4
                    WHEN 'ACCEPTED'    THEN 5
                    WHEN 'REFUSED'     THEN 6
                    ELSE 7
                END,
                r.issue_date DESC
            """;

    private final List<String> conditions = new ArrayList<>();
    private final Map<String, Object> params = new HashMap<>();

    // ─────────────────────────────────────────────────────────────
    // Department
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyDepartmentFilter(Long departmentId) {
        if (departmentId == null) return this;
        conditions.add("""
                AND r.author_id IN (
                    SELECT u.id FROM users u
                    JOIN profiles p ON p.id = u.id
                    WHERE p.department_id = :departmentId
                )""");
        params.put("departmentId", departmentId);
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Locations
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyFromLocationFilter(String country, String zip, String place) {
        return applyLocationFilter("from_location_ids_order", "f", "fromIso", "fromZip", "fromPlace",
                country, zip, place);
    }

    public RequestSqlBuilder applyToLocationFilter(String country, String zip, String place) {
        return applyLocationFilter("to_location_ids_order", "t", "toIso", "toZip", "toPlace",
                country, zip, place);
    }

    private RequestSqlBuilder applyLocationFilter(
            String column, String alias,
            String isoParam, String zipParam, String placeParam,
            String country, String zip, String place) {

        if (country == null && zip == null && place == null) return this;

        StringBuilder exists = new StringBuilder(
                "AND EXISTS (SELECT 1 FROM jsonb_array_elements_text(r." + column + ") AS " + alias + "(id) " +
                "JOIN location l ON l.id = " + alias + ".id::bigint WHERE 1=1");

        if (country != null) {
            exists.append(" AND l.country_code = :").append(isoParam);
            params.put(isoParam, country.toUpperCase());
        }
        if (zip != null) {
            exists.append(" AND l.zip_code LIKE :").append(zipParam);
            params.put(zipParam, zip + "%");
        }
        if (place != null) {
            exists.append(" AND COALESCE(l.place_name,'') ILIKE :").append(placeParam);
            params.put(placeParam, "%" + place + "%");
        }
        exists.append(")");
        conditions.add(exists.toString());
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Enum lists
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyStatusFilter(List<RequestStatusDto> statuses) {
        if (statuses == null || statuses.isEmpty()) return this;
        conditions.add("AND r.status IN :statuses");
        params.put("statuses", statuses.stream().map(Enum::name).toList());
        return this;
    }

    public RequestSqlBuilder applyRequestTypeFilter(List<String> types) {
        if (types == null || types.isEmpty()) return this;
        conditions.add("AND r.request_type IN :types");
        params.put("types", types);
        return this;
    }

    public RequestSqlBuilder applyShipmentTypeFilter(List<ShipmentTypeDto> types) {
        if (types == null || types.isEmpty()) return this;
        conditions.add("AND r.shipment_type IN :shipmentTypes");
        params.put("shipmentTypes", types.stream().map(Enum::name).toList());
        return this;
    }

    public RequestSqlBuilder applyTransportTypeFilter(List<TransportTypeDto> types) {
        if (types == null || types.isEmpty()) return this;
        conditions.add("AND r.transport_type IN :transportTypes");
        params.put("transportTypes", types.stream().map(Enum::name).toList());
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Company
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyCompanyFilter(Long companyId, String companyName) {
        if (companyId != null) {
            conditions.add("AND r.customer_company_id = :companyId");
            params.put("companyId", companyId);
        } else if (companyName != null && !companyName.isBlank()) {
            conditions.add("AND LOWER(c.name) LIKE :companyName");
            params.put("companyName", "%" + companyName.toLowerCase() + "%");
        }
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Dates
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyDateFilter(DatesFilterOption option, LocalDate start, LocalDate end) {
        if (option == null) return this;
        if (start == null && end == null) return this;
        if (start != null && end == null) end = start;

        String column = switch (option) {
            case REQUEST_ISSUE_DATE -> "issue_date";
            case LOADING_DATE       -> "start_date";
            case DELIVERY_DATE      -> "end_date";
        };
        if (start != null) {
            conditions.add("AND r." + column + "::date >= :startDate");
            params.put("startDate", start);
        }
        if (end != null) {
            conditions.add("AND r." + column + "::date <= :endDate");
            params.put("endDate", end);
        }
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Dangerous
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyDangerousFilter(DangerousFilterOption dangerous) {
        if (dangerous == null) return this;
        conditions.add(switch (dangerous) {
            case ADR     -> "AND r.dangerous = true";
            case NON_ADR -> "AND r.dangerous = false";
        });
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Weight & LDM
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyWeightFilter(Double min, Double max) {
        if (min != null) { conditions.add("AND r.weight >= :minWeight");       params.put("minWeight", min); }
        if (max != null) { conditions.add("AND r.weight <= :maxWeight");       params.put("maxWeight", max); }
        return this;
    }

    public RequestSqlBuilder applyLdmFilter(Double min, Double max) {
        if (min != null) { conditions.add("AND r.loading_meter >= :minLdm");  params.put("minLdm", min); }
        if (max != null) { conditions.add("AND r.loading_meter <= :maxLdm");  params.put("maxLdm", max); }
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Author / Competitor / Dispatcher
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyAuthorFilter(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return this;
        conditions.add("AND r.author_id IN :authorIds");
        params.put("authorIds", ids);
        return this;
    }

    public RequestSqlBuilder applyCompetitorFilter(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return this;
        conditions.add("""
                AND r.id IN (
                    SELECT rc.request_id FROM request_competitors rc
                    WHERE rc.user_id IN :competitorIds
                )""");
        params.put("competitorIds", ids);
        return this;
    }

    public RequestSqlBuilder applyDispatcherFilter(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return this;
        conditions.add("AND r.dispatcher_id IN :dispatchersIds");
        params.put("dispatchersIds", ids);
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Text search (searchByQuery)
    // ─────────────────────────────────────────────────────────────

    public RequestSqlBuilder applyTypeFilter(RequestTypeDto type) {
        if (type == null) return this;
        conditions.add("AND r.request_type = :requestType");
        params.put("requestType", type.name());
        return this;
    }

    public RequestSqlBuilder applyTextSearch(String query) {
        if (query == null || query.isBlank()) return this;
        conditions.add("""
                AND (
                    LOWER(COALESCE(r.customer_reference, '')) LIKE :query
                    OR LOWER(COALESCE(r.tid, ''))             LIKE :query
                    OR LOWER(COALESCE(a.name, '') || ' ' || COALESCE(a.surname, '')) LIKE :query
                    OR LOWER(COALESCE(c.name, ''))            LIKE :query
                    OR LOWER(COALESCE(r.comments, ''))        LIKE :query
                    OR LOWER(COALESCE(r.temperature, ''))     LIKE :query
                    OR EXISTS (
                        SELECT 1 FROM jsonb_array_elements_text(r.from_location_ids_order) AS f(id)
                        JOIN location l ON l.id = f.id::bigint
                        WHERE LOWER(COALESCE(l.zip_code::text, '')) LIKE :query
                           OR LOWER(COALESCE(l.place_name, ''))     LIKE :query
                    )
                    OR EXISTS (
                        SELECT 1 FROM jsonb_array_elements_text(r.to_location_ids_order) AS t(id)
                        JOIN location l ON l.id = t.id::bigint
                        WHERE LOWER(COALESCE(l.zip_code::text, '')) LIKE :query
                           OR LOWER(COALESCE(l.place_name, ''))     LIKE :query
                    )
                    OR CAST(r.id AS TEXT) LIKE :query
                )""");
        params.put("query", "%" + query.toLowerCase() + "%");
        return this;
    }

    // ─────────────────────────────────────────────────────────────
    // Build
    // ─────────────────────────────────────────────────────────────

    public String toSqlFragment() {
        if (conditions.isEmpty()) return BASE_FROM;
        return BASE_FROM + String.join("\n", conditions) + "\n";
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

}
package com.mayak.ietms.features.statistics.infra.persistence;

import com.mayak.ietms.features.company.domain.model.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CompanyStatisticsRepository extends Repository<Company, Long> {

    interface CompanyLaneRow {
        Long getCompanyId();
        String getCompanyName();
        String getLane();
        String getTransportType();

        Integer getSpotCount();
        Double getSpotEff();
        BigDecimal getSpotProfit();

        Integer getContractCount();
        Double getContractEff();
        BigDecimal getContractProfit();
    }

    @Query(value = """
        SELECT
            c.id                                                   AS companyId,
            c.name                                                 AS companyName,
            COALESCE(lf.country_code, 'N/A') || '-' || COALESCE(lt.country_code, 'N/A') AS lane,
            r.transport_type                                       AS transportType,

            -- spot
            SUM(CASE WHEN r.request_type = 'SPOT' THEN 1 ELSE 0 END) AS spotCount,
            COALESCE(
                100.0 * SUM(CASE WHEN r.request_type = 'SPOT' AND r.profit_margin > 0 THEN 1 ELSE 0 END)
                / NULLIF(SUM(CASE WHEN r.request_type = 'SPOT' THEN 1 ELSE 0 END), 0)
            , 0.0) AS spotEff,
            COALESCE(SUM(CASE WHEN r.request_type = 'SPOT' AND r.client_price IS NOT NULL AND r.bid_price IS NOT NULL
                              THEN (r.client_price - r.bid_price) ELSE 0 END), 0) AS spotProfit,

            -- contract
            SUM(CASE WHEN r.request_type = 'CONTRACT' THEN 1 ELSE 0 END) AS contractCount,
            COALESCE(
                100.0 * SUM(CASE WHEN r.request_type = 'CONTRACT' AND r.profit_margin > 0 THEN 1 ELSE 0 END)
                / NULLIF(SUM(CASE WHEN r.request_type = 'CONTRACT' THEN 1 ELSE 0 END), 0)
            , 0.0) AS contractEff,
            COALESCE(SUM(CASE WHEN r.request_type = 'CONTRACT' AND r.client_price IS NOT NULL AND r.bid_price IS NOT NULL
                              THEN (r.client_price - r.bid_price) ELSE 0 END), 0) AS contractProfit

        FROM requests r
        JOIN company c ON c.id = r.customer_company_id

        LEFT JOIN location lf ON lf.id = (r.from_location_ids_order->>0)::bigint
        LEFT JOIN location lt ON lt.id = (r.to_location_ids_order->>0)::bigint

        WHERE r.status = 'ACCEPTED'
          AND r.issue_date >= :from
          AND r.issue_date <= :to
          AND r.customer_company_id = ANY(:companyIds)

        GROUP BY c.id, c.name, lane, r.transport_type
        ORDER BY c.id, lane, r.transport_type
        """, nativeQuery = true)
    List<CompanyLaneRow> companyLaneStats(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("companyIds") Long[] companyIds
    );
}
package com.mayak.ietms.features.statistics.infra.persistence;

import com.mayak.ietms.features.company.domain.model.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CompanyLookupRepository extends Repository<Company, Long> {

    interface CompanyLookupRow {
        Long getCompanyId();
        String getCompanyName();
    }

    @Query(value = """
    SELECT DISTINCT
        c.id   AS companyId,
        c.name AS companyName
    FROM requests r
    JOIN users u      ON u.id = r.author_id
    JOIN profiles p   ON p.id = u.id
    JOIN company c    ON c.id = r.customer_company_id
    WHERE p.department_id = :departmentId
      AND r.issue_date >= :from
      AND r.issue_date < :toExclusive
    ORDER BY c.name
    """, nativeQuery = true)
    List<CompanyLookupRow> findActiveCompaniesForDepartment(
            @Param("departmentId") Long departmentId,
            @Param("from") Instant from,
            @Param("toExclusive") Instant toExclusive
    );
}
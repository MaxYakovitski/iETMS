package com.mayak.iet.features.statistics.infra.persistence;

import com.mayak.iet.features.company.domain.model.Company;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
      AND r.issue_date <= :to
    ORDER BY c.name
    """, nativeQuery = true)
    List<CompanyLookupRow> findActiveCompaniesForDepartment(
            @Param("departmentId") Long departmentId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

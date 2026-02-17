package com.mayak.ietms.integration.analytics;

import com.mayak.ietms.analytics.AnalyticsFilterDto;
import com.mayak.ietms.analytics.AnalyticsReportDto;
import com.mayak.ietms.company.dto.CompanyDto;

import java.time.LocalDate;
import java.util.List;

public interface AnalyticsClient {

    AnalyticsReportDto getAnalytics(AnalyticsFilterDto filter);

    List<CompanyDto> findCompaniesForDepartmentAnalytics(
            Long departmentId,
            LocalDate from,
            LocalDate to
    );
}
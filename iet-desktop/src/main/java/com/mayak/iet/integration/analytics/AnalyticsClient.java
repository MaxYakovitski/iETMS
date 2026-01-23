package com.mayak.iet.integration.analytics;

import com.mayak.iet.analytics.AnalyticsFilterDto;
import com.mayak.iet.analytics.AnalyticsReportDto;
import com.mayak.iet.company.dto.CompanyDto;

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
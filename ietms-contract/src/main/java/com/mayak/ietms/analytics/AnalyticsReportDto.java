package com.mayak.ietms.analytics;

import com.mayak.ietms.statistics.CompanyStatsDto;
import com.mayak.ietms.statistics.DepartmentStatsDto;
import com.mayak.ietms.statistics.UserStatsDto;

import java.util.List;

public record AnalyticsReportDto (
        List<CompanyStatsDto> companies,
        List<UserStatsDto> users,
        DepartmentStatsDto department) {
}
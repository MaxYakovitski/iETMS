package com.mayak.iet.analytics;

import com.mayak.iet.statistics.CompanyStatsDto;
import com.mayak.iet.statistics.DepartmentStatsDto;
import com.mayak.iet.statistics.UserStatsDto;

import java.util.List;

public record AnalyticsReportDto (
        List<CompanyStatsDto> companies,
        List<UserStatsDto> users,
        DepartmentStatsDto department) {
}
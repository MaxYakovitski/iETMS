package com.mayak.ietms.features.analytics.application;

import com.mayak.ietms.analytics.AnalyticsFilterDto;
import com.mayak.ietms.analytics.AnalyticsReportDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.statistics.application.CompanyStatisticsService;
import com.mayak.ietms.features.statistics.application.DepartmentStatisticsService;
import com.mayak.ietms.features.statistics.application.UserStatisticsService;
import com.mayak.ietms.statistics.CompanyStatsDto;
import com.mayak.ietms.statistics.DepartmentStatsDto;
import com.mayak.ietms.statistics.UserStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CompanyStatisticsService companyStats;
    private final UserStatisticsService userStats;
    private final DepartmentStatisticsService departmentStats;

    public AnalyticsReportDto buildAnalyticsReport(AnalyticsFilterDto filter) {

        ZoneId zone = ZoneOffset.UTC;

        Instant from = filter.start().atStartOfDay(zone).toInstant();
        Instant toExclusive = filter.end().plusDays(1).atStartOfDay(zone).toInstant();

        List<CompanyStatsDto> companies =
                companyStats.getCompanyReport(filter.start(), filter.end(), filter.companyIds());

        List<UserStatsDto> users =
                userStats.getUserStats(filter.start(), filter.end(), filter.userIds());

        DepartmentStatsDto department =
                departmentStats.getDepartmentStats(filter.departmentId(), from, toExclusive);

        return new AnalyticsReportDto(companies, users, department);
    }

    public List<CompanyDto> findCompaniesForDepartmentAnalytics(Long departmentId, LocalDate from, LocalDate to) {
        return companyStats.findCompaniesForDepartmentAnalytics(departmentId, from, to);
    }
}
package com.mayak.iet.ui.analytics.service;

import com.mayak.iet.integration.analytics.AnalyticsClient;
import com.mayak.iet.analytics.AnalyticsFilterDto;
import com.mayak.iet.analytics.AnalyticsReportDto;
import com.mayak.iet.statistics.DepartmentStatsDto;
import com.mayak.iet.statistics.MonthlyCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentAnalyticsFacade {

    private final AnalyticsClient analyticsClient;

    public DepartmentStatsDto loadDepartmentStats(LocalDate start, LocalDate end, Long departmentId) {
        AnalyticsFilterDto filter = new AnalyticsFilterDto(start, end, departmentId, null, null);

        AnalyticsReportDto report = analyticsClient.getAnalytics(filter);
        return report.department();
    }

    public List<MonthlyCountDto> loadMonthlyCompression(LocalDate start, LocalDate end, Long departmentId) {
        AnalyticsFilterDto filter = new AnalyticsFilterDto(start, end, departmentId, null, null);

        return analyticsClient.getAnalytics(filter)
                .department()
                .monthlyCompression();
    }
}
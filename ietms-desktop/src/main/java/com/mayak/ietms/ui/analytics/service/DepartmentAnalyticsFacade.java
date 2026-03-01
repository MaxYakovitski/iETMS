package com.mayak.ietms.ui.analytics.service;

import com.mayak.ietms.integration.analytics.AnalyticsClient;
import com.mayak.ietms.analytics.AnalyticsFilterDto;
import com.mayak.ietms.analytics.AnalyticsReportDto;
import com.mayak.ietms.statistics.DepartmentStatsDto;
import com.mayak.ietms.statistics.MonthlyCountDto;
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

    public List<MonthlyCountDto> loadMonthlyCompression(LocalDate now, Long departmentId) {

        LocalDate start = now.minusMonths(12).withDayOfMonth(1);;
        LocalDate end = now;
        AnalyticsFilterDto filter = new AnalyticsFilterDto(start, end, departmentId, null, null);

        return analyticsClient.getAnalytics(filter)
                .department()
                .monthlyCompression();
    }
}
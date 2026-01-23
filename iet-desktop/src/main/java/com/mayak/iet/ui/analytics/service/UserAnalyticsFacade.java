package com.mayak.iet.ui.analytics.service;

import com.mayak.iet.integration.analytics.AnalyticsClient;
import com.mayak.iet.analytics.AnalyticsFilterDto;
import com.mayak.iet.analytics.AnalyticsReportDto;
import com.mayak.iet.statistics.UserStatsDto;
import com.mayak.iet.user.dto.UserLookupDto;
import com.mayak.iet.ui.analytics.model.UserReport;
import com.mayak.iet.ui.analytics.model.UserReportItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAnalyticsFacade {

    private final AnalyticsClient analyticsClient;

    public List<UserReport> loadUserReports(LocalDate start, LocalDate end, Long departmentId, List<Long> userIds) {
        AnalyticsFilterDto filter = new AnalyticsFilterDto(start, end, departmentId, null, userIds);

        AnalyticsReportDto report = analyticsClient.getAnalytics(filter);

        return report.users().stream().map(this::map).toList();
    }

    private UserReport map(UserStatsDto dto) {
        return new UserReport(
                new UserLookupDto(dto.userId(), dto.name()),
                List.of(new UserReportItem(
                        dto.placed(),
                        dto.joined(),
                        dto.bided(),
                        dto.assigned(),
                        dto.avgResponseMinutes()
                ))
        );
    }
}

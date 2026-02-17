package com.mayak.ietms.ui.analytics.service;

import com.mayak.ietms.integration.analytics.AnalyticsClient;
import com.mayak.ietms.analytics.AnalyticsFilterDto;
import com.mayak.ietms.analytics.AnalyticsReportDto;
import com.mayak.ietms.statistics.UserStatsDto;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.ui.analytics.model.UserReport;
import com.mayak.ietms.ui.analytics.model.UserReportItem;
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
                        dto.dispatched(),
                        dto.avgResponseMinutes()
                ))
        );
    }
}

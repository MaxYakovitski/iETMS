package com.mayak.ietms.analytics;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsFilterDto(
        LocalDate start,
        LocalDate end,
        Long departmentId,
        List<Long> companyIds,
        List<Long> userIds) {
}
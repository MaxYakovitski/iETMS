package com.mayak.iet.ui.analytics.model;

import java.math.BigDecimal;

public record UserReportItem(
        int placed,
        int joined,
        int bided,
        int dispatched,
        BigDecimal avgResponse) {
}
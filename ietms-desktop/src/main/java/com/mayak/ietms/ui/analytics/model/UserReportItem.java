package com.mayak.ietms.ui.analytics.model;

import java.math.BigDecimal;

public record UserReportItem(
        int placed,
        int joined,
        int bided,
        int accepted,
        int dispatched,
        BigDecimal avgResponse) {
}
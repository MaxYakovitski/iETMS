package com.mayak.ietms.shared.statistics;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;

public enum PeriodType {
    CURRENT_MONTH;

    public Instant getStart() {
        return YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
    public Instant getEnd() {
        return Instant.now();
    }
}
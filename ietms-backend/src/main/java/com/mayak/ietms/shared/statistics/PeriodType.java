package com.mayak.ietms.shared.statistics;

import java.time.LocalDateTime;
import java.time.YearMonth;

public enum PeriodType {
    CURRENT_MONTH;

    public LocalDateTime getStart() {
        return YearMonth.now().atDay(1).atStartOfDay();
    }
    public LocalDateTime getEnd() {
        return LocalDateTime.now();
    }
}
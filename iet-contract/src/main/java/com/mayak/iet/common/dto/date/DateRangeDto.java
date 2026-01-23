package com.mayak.iet.common.dto.date;

import java.time.LocalDate;

public record DateRangeDto(
        LocalDate startDate,
        LocalDate endDate) {
}
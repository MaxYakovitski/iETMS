package com.mayak.ietms.common.dto.date;

import java.time.LocalDate;

public record DateRangeDto(
        LocalDate startDate,
        LocalDate endDate) {
}
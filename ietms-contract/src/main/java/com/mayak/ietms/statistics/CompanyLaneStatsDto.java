package com.mayak.ietms.statistics;

import java.math.BigDecimal;

public record CompanyLaneStatsDto(
        String lane,
        String transportType,
        int spotCount,
        double spotEfficiencyPercent,
        BigDecimal spotProfit,
        int contractCount,
        double contractEfficiencyPercent,
        BigDecimal contractProfit) {
}
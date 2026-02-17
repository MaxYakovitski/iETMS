package com.mayak.ietms.ui.analytics.model;

import java.math.BigDecimal;

public record CompanyReportItem(
        String lane,
        String transportType,
        int spot,
        double spotEfficiency,
        BigDecimal spotMargin,
        int contract,
        double contractEfficiency,
        BigDecimal contractMargin) {
}
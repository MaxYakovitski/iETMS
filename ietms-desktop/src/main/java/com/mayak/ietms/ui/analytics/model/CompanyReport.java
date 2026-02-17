package com.mayak.ietms.ui.analytics.model;

import com.mayak.ietms.company.dto.CompanyDto;

import java.util.List;

public record CompanyReport(
        CompanyDto company,
        List<CompanyReportItem> items) {
}
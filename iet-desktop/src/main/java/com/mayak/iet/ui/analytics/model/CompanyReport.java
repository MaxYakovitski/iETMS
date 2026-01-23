package com.mayak.iet.ui.analytics.model;

import com.mayak.iet.company.dto.CompanyDto;

import java.util.List;

public record CompanyReport(
        CompanyDto company,
        List<CompanyReportItem> items) {
}
package com.mayak.ietms.integration.api;

import com.mayak.ietms.statistics.ReportType;

import java.io.File;
import java.time.LocalDate;

public interface ReportClient {
    void downloadRequestsReport(
            ReportType type,
            LocalDate from,
            LocalDate to,
            File targetFile
    );
}
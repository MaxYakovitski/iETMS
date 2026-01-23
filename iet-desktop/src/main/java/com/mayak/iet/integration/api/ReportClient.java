package com.mayak.iet.integration.api;

import java.io.File;
import java.time.LocalDate;

public interface ReportClient {
    void downloadRequestsReport(
            LocalDate from,
            LocalDate to,
            File targetFile
    );
}
package com.mayak.ietms.features.report.api;

import com.mayak.ietms.features.report.application.ReportExportService;
import com.mayak.ietms.features.request.application.RequestQueryService;
import com.mayak.ietms.features.statistics.application.UserStatisticsService;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.statistics.ReportType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAuthority('VIEW_ANALYTICS')")
@Tag(name = "Reports", description = "Excel report export")
@RequiredArgsConstructor
public class ReportController {

    private final RequestQueryService requestQueryService;
    private final UserStatisticsService userStatisticsService;
    private final ReportExportService reportExportService;

    @GetMapping(value = "/requests.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export requests report",
               description = "Returns an Excel (.xlsx) file. Report type: BY_DEPARTMENT — requests grouped by department, " +
                       "BY_EMPLOYEES — statistics per employee. Date params must be in ISO format (yyyy-MM-dd).")
    public void exportRequests(
            @RequestParam("type") ReportType type,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @CurrentUserId Long userId,
            HttpServletResponse response
    ) throws IOException {

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=report_" + type + "_" + start + "_to_" + end + ".xlsx");

        switch (type) {
            case BY_DEPARTMENT -> exportRequests(response, start, end,  userId);
            case BY_EMPLOYEES -> exportByEmployees(response, start, end, userId);

        }

    }

    private void exportRequests(HttpServletResponse response, LocalDate start, LocalDate end, Long userId) throws IOException {
        var data = requestQueryService.findRequestsForReport(start, end,  userId);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        reportExportService.exportRequestsToExcel(data, buffer);

        byte[] bytes = buffer.toByteArray();
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private void exportByEmployees(HttpServletResponse response, LocalDate start, LocalDate end, Long userId) throws IOException {
        var stats = userStatisticsService.getUserStats(start, end, userId);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        reportExportService.exportUserStatsToExcel(stats, buffer);

        byte[] bytes = buffer.toByteArray();
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }
}
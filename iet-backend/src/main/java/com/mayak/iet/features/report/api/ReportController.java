package com.mayak.iet.features.report.api;

import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.report.application.ReportExportService;
import com.mayak.iet.features.request.application.RequestQueryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final RequestQueryService requestQueryService;
    private final ReportExportService reportExportService;

    @GetMapping(value = "/requests.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void exportRequests(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            HttpServletResponse response
    ) throws IOException {

        LocalDate start = LocalDate.parse(from);
        LocalDate end = LocalDate.parse(to);

        response.setHeader("Content-Disposition",
                "attachment; filename=report_" + start + "_to_" + end + ".xlsx");

        List<Request> data = requestQueryService.findRequestsForReport(start, end);
        reportExportService.exportRequestsToExcel(data, response.getOutputStream());
    }
}
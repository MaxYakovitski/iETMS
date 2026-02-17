package com.mayak.ietms.features.analytics.api;

import com.mayak.ietms.analytics.AnalyticsFilterDto;
import com.mayak.ietms.analytics.AnalyticsReportDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.features.analytics.application.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/report")
    public AnalyticsReportDto getAnalytics(@RequestBody AnalyticsFilterDto filter) {
        return analyticsService.buildAnalyticsReport(filter);
    }

    @GetMapping("/companies-for-department")
    public List<CompanyDto> findCompaniesForDepartmentAnalytics(
            @RequestParam("departmentId") Long departmentId,
            @RequestParam("from") LocalDate from,
            @RequestParam("to") LocalDate to
    ) {
        return analyticsService.findCompaniesForDepartmentAnalytics(
                departmentId, from, to
        );
    }
}
package com.mayak.iet.statistics;

import java.util.List;

public record CompanyStatsDto(
        Long companyId,
        String companyName,
        List<CompanyLaneStatsDto> items) {
}
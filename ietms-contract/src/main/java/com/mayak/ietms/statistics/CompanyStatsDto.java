package com.mayak.ietms.statistics;

import java.util.List;

public record CompanyStatsDto(
        Long companyId,
        String companyName,
        List<CompanyLaneStatsDto> items) {
}
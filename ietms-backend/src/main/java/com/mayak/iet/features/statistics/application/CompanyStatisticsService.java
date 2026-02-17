package com.mayak.iet.features.statistics.application;

import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.statistics.CompanyLaneStatsDto;
import com.mayak.iet.statistics.CompanyStatsDto;
import com.mayak.iet.features.statistics.infra.persistence.CompanyLookupRepository;
import com.mayak.iet.features.statistics.infra.persistence.CompanyStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyStatisticsService {

    private final CompanyStatisticsRepository repo;
    private final CompanyLookupRepository lookupRepo;

    public List<CompanyStatsDto> getCompanyReport(LocalDate start, LocalDate end, List<Long> companyIds) {
        if (companyIds == null || companyIds.isEmpty()) return List.of();

        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.atTime(LocalTime.MAX);

        var rows = repo.companyLaneStats(from, to, companyIds.toArray(Long[]::new));

        Map<Long, List<CompanyStatisticsRepository.CompanyLaneRow>> grouped = new LinkedHashMap<>();
        for (var r : rows) {
            grouped.computeIfAbsent(r.getCompanyId(), k -> new ArrayList<>()).add(r);
        }

        List<CompanyStatsDto> result = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            var list = entry.getValue();
            var first = list.getFirst();

            List<CompanyLaneStatsDto> items = list.stream()
                    .map(r -> new CompanyLaneStatsDto(
                            r.getLane(),
                            r.getTransportType(),
                            nz(r.getSpotCount()),
                            nz(r.getSpotEff()),
                            nzMoney(r.getSpotProfit()),
                            nz(r.getContractCount()),
                            nz(r.getContractEff()),
                            nzMoney(r.getContractProfit())
                    ))
                    .toList();

            result.add(new CompanyStatsDto(first.getCompanyId(), first.getCompanyName(), items));
        }

        return result;
    }

    public List<CompanyDto> findCompaniesForDepartmentAnalytics(Long departmentId, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end   = to.atTime(LocalTime.MAX);

        return lookupRepo.findActiveCompaniesForDepartment(departmentId, start, end)
                .stream()
                .map(r -> new CompanyDto(r.getCompanyId(), r.getCompanyName()))
                .toList();
    }

    private int nz(Integer v) { return v == null ? 0 : v; }
    private double nz(Double v) { return v == null ? 0.0 : v; }
    private BigDecimal nzMoney(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
package com.mayak.ietms.statistics;

import java.util.List;

public record DepartmentStatsDto(
        int spotTotal,
        int contractTotal,

        int spotAccepted,
        int contractAccepted,

        int spotRefused,
        int contractRefused,

        int spotBided,
        int spotNotBided,

        List<RefuseReasonCountDto> spotRefusedByReason,
        List<RefuseReasonCountDto> contractRefusedByReason,
        List<MonthlyCountDto> monthlyCompression) {

    public static DepartmentStatsDto empty() {
        return new DepartmentStatsDto(
                0, 0,
                0, 0,
                0, 0,
                0, 0,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
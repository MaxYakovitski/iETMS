package com.mayak.ietms.statistics;

public record MonthlyCountDto(
        String month,
        int spot,
        int contract
) {
}
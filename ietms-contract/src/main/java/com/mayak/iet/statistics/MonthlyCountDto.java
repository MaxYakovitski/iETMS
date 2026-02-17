package com.mayak.iet.statistics;

public record MonthlyCountDto(
        String month,
        int spot,
        int contract
) {
}
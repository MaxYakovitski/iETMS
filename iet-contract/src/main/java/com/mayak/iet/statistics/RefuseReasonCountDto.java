package com.mayak.iet.statistics;

public record RefuseReasonCountDto(
        String reasonCode,
        int count
) {
}
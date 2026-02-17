package com.mayak.ietms.statistics;

public record RefuseReasonCountDto(
        String reasonCode,
        int count
) {
}
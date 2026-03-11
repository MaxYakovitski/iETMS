package com.mayak.ietms.statistics;

import com.mayak.ietms.user.dto.UserNameDto;

import java.math.BigDecimal;

public record UserStatsDto(
        Long userId,
        UserNameDto name,
        int placed,
        int joined,
        int bided,
        int acceptedSpot,
        int acceptedContract,
        int dispatched,
        BigDecimal avgResponseMinutes) {
}
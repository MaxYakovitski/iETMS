package com.mayak.iet.statistics;

import com.mayak.iet.user.dto.UserNameDto;

import java.math.BigDecimal;

public record UserStatsDto(
        Long userId,
        UserNameDto name,
        int placed,
        int joined,
        int bided,
        int assigned,
        BigDecimal avgResponseMinutes) {
}
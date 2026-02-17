package com.mayak.ietms.request.dto.bid;

import com.mayak.ietms.user.dto.UserLookupDto;

import java.math.BigDecimal;
import java.time.Instant;

public record BidViewDto(
        Long id,
        Long userId,
        UserLookupDto user,
        BigDecimal amount,
        Instant time,
        String comment,
        boolean deleted) {
}
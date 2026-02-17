package com.mayak.ietms.request.dto.bid;

import com.mayak.ietms.user.dto.UserLookupDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidViewDto(
        Long id,
        Long userId,
        UserLookupDto user,
        BigDecimal amount,
        LocalDateTime time,
        String comment,
        boolean deleted) {
}
package com.mayak.ietms.request.dto.bid;

import java.math.BigDecimal;

public record BidCreateDto(
        Long requestId,
        BigDecimal amount,
        String comment) {
}
package com.mayak.iet.request.dto.bid;

import java.math.BigDecimal;

public record BidCreateDto(
        Long requestId,
        BigDecimal amount,
        String comment) {
}
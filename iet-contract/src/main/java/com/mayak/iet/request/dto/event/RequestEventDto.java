package com.mayak.iet.request.dto.event;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;

import java.math.BigDecimal;

public record RequestEventDto (
        RequestTypeDto type,
        RequestStatusDto status,
        BigDecimal clientPrice,
        BigDecimal bidPrice,
        BigDecimal profit) {
}
package com.mayak.ietms.request.dto.event;

import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;

import java.math.BigDecimal;

public record RequestEventDto (
        RequestTypeDto type,
        RequestStatusDto status,
        Long dispatcherId,
        BigDecimal clientPrice,
        BigDecimal bidPrice,
        BigDecimal profit) {
}
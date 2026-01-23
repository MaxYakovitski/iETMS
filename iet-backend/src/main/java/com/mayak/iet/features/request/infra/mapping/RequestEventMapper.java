package com.mayak.iet.features.request.infra.mapping;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.event.RequestEventDto;
import com.mayak.iet.features.request.domain.model.Request;
import org.springframework.stereotype.Component;

@Component
public class RequestEventMapper {
    public RequestEventDto toEventDto(Request req) {
        if (req == null) return null;

        return new RequestEventDto(
                mapType(req),
                mapStatus(req),
                req.getClientPrice(),
                req.getBidPrice(),
                req.getProfitMargin()
        );
    }

    private RequestStatusDto mapStatus(Request req) {
        if (req.getStatus() == null) return null;
        return RequestStatusDto.valueOf(req.getStatus().name());
    }

    private RequestTypeDto mapType(Request req) {
        return req.getRequestTypeDto();
    }
}
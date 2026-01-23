package com.mayak.iet.features.request.infra.mapping;

import com.mayak.iet.features.request.domain.enums.ContractReasonCode;
import com.mayak.iet.features.request.domain.enums.ReasonCode;
import com.mayak.iet.features.request.domain.enums.SpotReasonCode;
import com.mayak.iet.features.request.domain.model.RefuseReason;
import com.mayak.iet.request.dto.enums.ContractReasonCodeDto;
import com.mayak.iet.request.dto.enums.SpotReasonCodeDto;
import org.springframework.stereotype.Component;

@Component
public class RefuseReasonMapper {

    public RefuseReason fromCode(String code) {
        if (code == null || code.isBlank()) return null;

        // 1. common / shared reasons
        try {
            return ReasonCode.valueOf(code);
        } catch (IllegalArgumentException ignored) {}

        // 2. contract reasons
        try {
            return ContractReasonCode.valueOf(code);
        } catch (IllegalArgumentException ignored) {}

        // 3. spot reasons
        try {
            return SpotReasonCode.valueOf(code);
        } catch (IllegalArgumentException ignored) {}

        return null;
    }

    public RefuseReason fromContractDto(ContractReasonCodeDto dto) {
        if (dto == null) return null;

        return switch (dto) {
            case NO_CORRESPONDING_TRUCK -> ContractReasonCode.NO_CORRESPONDING_TRUCK;
        };
    }

    public RefuseReason fromSpotDto(SpotReasonCodeDto dto) {
        if (dto == null) return null;

        return switch (dto) {
            case OFFERED_NOT_ON_TIME -> SpotReasonCode.OFFERED_NOT_ON_TIME;
            case BAD_TRANSIT_TIME -> SpotReasonCode.BAD_TRANSIT_TIME;
        };
    }
}
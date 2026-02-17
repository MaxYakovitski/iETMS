package com.mayak.iet.features.request.application.factory;

import com.mayak.iet.features.request.infra.mapping.RefuseReasonMapper;
import com.mayak.iet.request.dto.create.BaseRequestDto;
import com.mayak.iet.request.dto.create.ContractRequestDto;
import com.mayak.iet.request.dto.create.SpotRequestDto;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.infra.mapping.ContractRequestMapper;
import com.mayak.iet.features.request.infra.mapping.SpotRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestFactory {

    private final SpotRequestMapper spotMapper;
    private final ContractRequestMapper contractMapper;
    private final RefuseReasonMapper refuseReasonMapper;

    public Request createRequest(BaseRequestDto dto) {
        if (dto == null || dto.getType() == null) {
            throw new IllegalArgumentException("Request type must be provided");
        }

        if (dto instanceof SpotRequestDto spot) {
            return spotMapper.toEntity(spot, refuseReasonMapper);
        }
        if (dto instanceof ContractRequestDto contract) {
            return contractMapper.toEntity(contract, refuseReasonMapper);
        }

        throw new IllegalStateException("Unsupported DTO class: " + dto.getClass());
    }
}
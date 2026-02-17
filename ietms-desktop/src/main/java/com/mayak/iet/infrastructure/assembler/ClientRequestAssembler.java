package com.mayak.iet.infrastructure.assembler;

import com.mayak.iet.request.dto.create.BaseRequestDto;
import com.mayak.iet.request.dto.create.ContractRequestDto;
import com.mayak.iet.request.dto.create.SpotRequestDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.ui.workspace.request.form.ClientRequestFormState;
import com.mayak.iet.infrastructure.common.TextUtils;
import com.mayak.iet.common.util.formatting.LocationTextParser;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClientRequestAssembler {

    public static BaseRequestDto build(ClientRequestFormState s) {
        if (s.getContract() == null) return null;

        RequestTypeDto type = s.isContract() ? RequestTypeDto.CONTRACT : RequestTypeDto.SPOT;

        BaseRequestDto dto = switch (type) {
            case CONTRACT -> {
                ContractRequestDto c = new ContractRequestDto();
                c.setLaneId(s.effectiveLaneId());
                yield c;
            }
            case SPOT -> new SpotRequestDto();
        };

        dto.setType(type);

        dto.setFromLocations(LocationTextParser.parseLocations(s.getFrom()));
        dto.setToLocations(LocationTextParser.parseLocations(s.getTo()));

        dto.setCustomerReference(TextUtils.safeTrim(s.getCustomerReference()));
        dto.setCustomerName(TextUtils.safeTrim(s.getCompanyName()));

        dto.setStartDate(s.getStartDate() != null ? s.getStartDate().atStartOfDay() : null);
        dto.setEndDate(s.getEndDate() != null ? s.getEndDate().atStartOfDay() : null);

        dto.setShipmentType(s.getShipmentType());
        dto.setTransportType(s.getTransportType());

        dto.setTemperature(s.isRef() ? TextUtils.safeTrim(s.getTemperature()) : null);
        dto.setDangerous(Boolean.TRUE.equals(s.getDangerous()));
        dto.setWeight(TextUtils.parseDoubleSafe(s.getWeight()));
        dto.setLoadingMeter(s.isLtl() ? TextUtils.parseDoubleSafe(s.getLoadingMeter()) : null);

        dto.setComments(TextUtils.safeTrim(s.getComments()));

        return dto;
    }
}
package com.mayak.iet.features.request.application.reason;

import com.mayak.iet.features.request.domain.enums.ContractReasonCode;
import com.mayak.iet.features.request.domain.enums.ReasonCode;
import com.mayak.iet.features.request.domain.enums.SpotReasonCode;
import com.mayak.iet.features.request.domain.model.ContractRequest;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.domain.model.SpotRequest;
import com.mayak.iet.request.dto.refuse.RefuseReasonOptionDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RefuseReasonResolver {

    public List<RefuseReasonOptionDto> resolve(Request request) {
        List<RefuseReasonOptionDto> result = new ArrayList<>();

        // common
        for (ReasonCode code : ReasonCode.values()) {
            if (code.isUserSelectable()) {
                result.add(new RefuseReasonOptionDto(code.name(), code.getLabel()));
            }
        }

        if (request instanceof SpotRequest) {
            for (SpotReasonCode code : SpotReasonCode.values()) {
                result.add(new RefuseReasonOptionDto(code.name(), code.getLabel()));
            }
        }

        if (request instanceof ContractRequest) {
            for (ContractReasonCode code : ContractReasonCode.values()) {
                result.add(new RefuseReasonOptionDto(code.name(), code.getLabel()));
            }
        }

        return result;
    }
}
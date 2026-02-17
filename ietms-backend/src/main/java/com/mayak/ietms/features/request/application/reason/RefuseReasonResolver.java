package com.mayak.ietms.features.request.application.reason;

import com.mayak.ietms.features.request.domain.enums.ContractReasonCode;
import com.mayak.ietms.features.request.domain.enums.ReasonCode;
import com.mayak.ietms.features.request.domain.enums.SpotReasonCode;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.request.dto.refuse.RefuseReasonOptionDto;
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
                result.add(new RefuseReasonOptionDto(code.name(), code.getCode()));
            }
        }

        if (request instanceof SpotRequest) {
            for (SpotReasonCode code : SpotReasonCode.values()) {
                result.add(new RefuseReasonOptionDto(code.name(), code.getCode()));
            }
        }

        if (request instanceof ContractRequest) {
            for (ContractReasonCode code : ContractReasonCode.values()) {
                result.add(new RefuseReasonOptionDto(code.name(), code.getCode()));
            }
        }

        return result;
    }
}
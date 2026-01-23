package com.mayak.iet.features.request.domain.model;

import com.mayak.iet.features.request.domain.enums.ReasonCode;
import com.mayak.iet.features.request.domain.enums.SpotReasonCode;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SPOT")
public class SpotRequest extends Request {

    @Override
    public void setReason(RefuseReason reason) {
        if (reason instanceof ReasonCode) {
            this.refuseReason = reason.getCode();
            return;
        }

        if (reason instanceof SpotReasonCode) {
            this.refuseReason = reason.getCode();
            return;
        }

        throw new IllegalArgumentException("Unsupported refuse reason for SpotRequest: " + reason);
    }
}

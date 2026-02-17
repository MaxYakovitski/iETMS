package com.mayak.ietms.features.request.domain.model;

import com.mayak.ietms.features.lane.domain.model.Lane;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CONTRACT")
public class ContractRequest extends Request {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lane_id")
    private Lane lane;

    public void assignLane(Lane lane) {
        if (lane == null) {
            throw new IllegalArgumentException("Lane must not be null");
        }
        this.lane = lane;
        this.setClientPrice(lane.getTotalPrice());
    }

    public BigDecimal getTotalPrice() {
        return lane != null ? lane.getTotalPrice() : null;
    }
}

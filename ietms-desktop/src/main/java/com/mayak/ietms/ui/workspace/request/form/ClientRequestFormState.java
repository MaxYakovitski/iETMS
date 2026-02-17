package com.mayak.ietms.ui.workspace.request.form;

import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.common.util.formatting.LocationTextFormatter;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ClientRequestFormState {

    private Boolean contract;
    private String from;
    private String to;

    private String customerReference;
    private String companyName;

    private LocalDate startDate;
    private LocalDate endDate;

    private ShipmentTypeDto shipmentType;
    private TransportTypeDto transportType;

    private String temperature;
    private Boolean dangerous;
    private String weight;
    private String loadingMeter;

    private String comments;

    private LaneViewDto lane;
    private Long inheritedLaneId;

    /* ========= invariants ========= */

    public boolean isContract() {
        return Boolean.TRUE.equals(contract);
    }

    public void setLane(LaneViewDto lane) {
        this.lane = lane;
        if (lane != null) {
            this.shipmentType = lane.shipmentType();
            this.transportType = lane.transportType();
            this.from = LocationTextFormatter.format(lane.fromLocation());
            this.to   = LocationTextFormatter.format(lane.toLocation());
            this.weight = lane.weight().toString();
            this.temperature = lane.temperature();
        }
    }

    public boolean hasLane() {
        return lane != null || inheritedLaneId != null;
    }

    public Long effectiveLaneId() {
        if (lane != null) return lane.id();
        return inheritedLaneId;
    }

    public boolean isLtl() {
        return shipmentType == ShipmentTypeDto.LTL;
    }

    public boolean isRef() {
        return transportType == TransportTypeDto.REF;
    }

    public void setShipmentType(ShipmentTypeDto type) {
        this.shipmentType = type;

        if (type != ShipmentTypeDto.LTL) {
            this.loadingMeter = null;
        }
    }

    public void setTransportType(TransportTypeDto type) {
        this.transportType = type;

        if (type != TransportTypeDto.REF) {
            this.temperature = null;
        }
    }

    public LocalDate getLaneValidFrom() {
        if (lane != null) {
            return lane.validFrom();
        }
        return null;
    }

    public LocalDate getLaneValidTo() {
        if (lane != null) {
            return lane.validTo();
        }
        return null;
    }

    public void clearLane() {
        this.lane = null;
        this.inheritedLaneId = null;
    }

    public void reset() {
        contract = null;
        from = null;
        to = null;
        customerReference = null;
        companyName = null;
        startDate = null;
        endDate = null;
        shipmentType = null;
        transportType = null;
        temperature = null;
        dangerous = null;
        weight = null;
        loadingMeter = null;
        comments = null;
        lane = null;
        inheritedLaneId = null;
    }
}
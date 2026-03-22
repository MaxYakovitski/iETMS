package com.mayak.ietms.domain.request.client;

import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.ui.workspace.request.form.ClientRequestFormState;
import com.mayak.ietms.common.util.formatting.LocationTextFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRequestPolicy {

    public void onRequestTypeChanged(ClientRequestFormState state, boolean contract) {
        state.setContract(contract);

        if (!contract) {
            state.clearLane();
        }
    }

    public void onLaneSelected(ClientRequestFormState state, LaneViewDto lane) {
        if (!state.isContract()) {
            throw new IllegalStateException("Lane for non-contract request");
        }
        state.setLane(lane);
    }

    public void onShipmentTypeChanged(ClientRequestFormState state, ShipmentTypeDto type) {
        if (state.hasLane()) return;
        state.setShipmentType(type);

    }

    public void onTransportTypeChanged(ClientRequestFormState state, TransportTypeDto type) {
        if (state.hasLane()) return;
        state.setTransportType(type);
    }

    public void onCompanyChanged(ClientRequestFormState state) {
        if (state.hasLane()) {
            state.setLane(null);
        }
    }

    public void applyRequest(ClientRequestFormState state, RequestDetailsDto r) {

        // 0. reset
        state.reset();

        // 1. request type
        boolean isContract = r.requestType() != RequestTypeDto.SPOT;
        state.setContract(isContract);

        // 2. base fields
        state.setFrom(LocationTextFormatter.formatAll(r.fromLocations()));
        state.setTo(LocationTextFormatter.formatAll(r.toLocations()));

        state.setCustomerReference(r.customerReference());
        state.setCompanyName(r.customer() != null ? r.customer().name() : null);

        state.setStartDate(r.startDate() != null ? r.startDate().toLocalDate() : null);
        state.setEndDate(r.endDate() != null ? r.endDate().toLocalDate() : null);

        state.setShipmentType(r.shipmentType());
        state.setTransportType(r.transportType());

        state.setWeight(r.weight() != null ? r.weight().toString() : null);

        state.setLoadingMeter(r.loadingMeter() != null ? r.loadingMeter().toString() : null);

        state.setTemperature(r.temperature());
        state.setDangerous(r.dangerous());
        state.setComments(r.comments());

        if (r.requestType() == RequestTypeDto.CONTRACT) {
            state.setStartDate(null);
            state.setEndDate(null);
            state.resolveLane(r.laneId(), r.laneValidFrom(), r.laneValidTo());
        }

    }
}
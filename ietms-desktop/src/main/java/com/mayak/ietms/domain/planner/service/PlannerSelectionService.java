package com.mayak.ietms.domain.planner.service;

import com.mayak.ietms.domain.planner.model.ShipmentContext;
import com.mayak.ietms.shipment.dto.enums.TransportEventType;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.ui.workspace.planner.enums.ActiveTab;
import com.mayak.ietms.ui.workspace.planner.state.PlannerState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PlannerSelectionService {

    public boolean isShipmentTab(PlannerState state) {
        return state.getActiveTab() == ActiveTab.MY_SHIPMENTS;
    }

    public boolean isTransportTab(PlannerState state) {
        return state.getActiveTab() == ActiveTab.MY_TRANSPORTS;
    }

    public ShipmentContext buildContext(ShipmentListItemDto dto, LocalDate selectedDate, TransportEventType eventType) {
        return ShipmentContext.from(
                dto,
                selectedDate,
                eventType
        );
    }
}
package com.mayak.iet.domain.planner.service;

import com.mayak.iet.domain.planner.model.ShipmentContext;
import com.mayak.iet.domain.planner.policy.ShipmentExecutionPolicy;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.ui.workspace.planner.enums.ActiveTab;
import com.mayak.iet.ui.workspace.planner.state.PlannerState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PlannerSelectionService {

    private final ShipmentExecutionPolicy executionPolicy;

    public boolean shouldShowDetails(ShipmentListItemDto dto) {
        return executionPolicy.existsAsOfDate(dto);
    }

    public boolean isShipmentTab(PlannerState state) {
        return state.getActiveTab() == ActiveTab.MY_SHIPMENTS;
    }

    public boolean isTransportTab(PlannerState state) {
        return state.getActiveTab() == ActiveTab.MY_TRANSPORTS;
    }

    public ShipmentContext buildContext(ShipmentListItemDto dto, LocalDate selectedDate) {
        return new ShipmentContext(
                dto.status(),
                selectedDate,
                dto.startDate().toLocalDate(),
                dto.endDate().toLocalDate()
        );
    }
}
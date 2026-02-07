package com.mayak.iet.ui.workspace.planner.state;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.ui.workspace.planner.enums.ActiveTab;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlannerState {

    private LocalDate selectedDate = LocalDate.now();
    private ShipmentListItemDto selectedShipment;

    private ActiveTab activeTab;

    private boolean hasMyShipments;
    private boolean hasMyTransports;
}
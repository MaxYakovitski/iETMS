package com.mayak.ietms.ui.workspace.planner.state;

import com.mayak.ietms.shipment.dto.enums.TransportEventType;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.ui.workspace.planner.enums.ActiveTab;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlannerState {

    private LocalDate selectedDate = LocalDate.now();
    private ShipmentListItemDto selectedShipment;

    private ActiveTab activeTab;

    private TransportEventType selectedTransportEvent;

    private boolean hasMyShipments;
    private boolean hasMyTransports;
}
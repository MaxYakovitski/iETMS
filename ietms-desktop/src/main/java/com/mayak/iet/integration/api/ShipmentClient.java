package com.mayak.iet.integration.api;

import com.mayak.iet.shipment.dto.enums.ShipmentCancelReasonDto;
import com.mayak.iet.shipment.dto.view.MyTransportEventDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentUpdateDto;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentClient {
    ShipmentListItemDto update(ShipmentUpdateDto dto);
    List<ShipmentListItemDto> findMyShipments(LocalDate date);
    List<MyTransportEventDto> findMyTransportEvents(LocalDate date);
    void cancel(long shipmentId, ShipmentCancelReasonDto reason);
}
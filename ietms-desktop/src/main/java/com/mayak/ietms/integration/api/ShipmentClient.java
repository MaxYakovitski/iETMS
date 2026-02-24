package com.mayak.ietms.integration.api;

import com.mayak.ietms.shipment.dto.enums.ShipmentCancelReasonDto;
import com.mayak.ietms.shipment.dto.view.MyTransportEventDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.shipment.dto.view.ShipmentUpdateDto;

import java.time.LocalDate;
import java.util.List;

public interface ShipmentClient {
    ShipmentListItemDto update(ShipmentUpdateDto dto);
    ShipmentListItemDto getShipment(Long shipmentId);
    List<ShipmentListItemDto> findMyShipments(LocalDate date);
    List<MyTransportEventDto> findMyTransportEvents(LocalDate date);
    void cancel(long shipmentId, ShipmentCancelReasonDto reason);
}
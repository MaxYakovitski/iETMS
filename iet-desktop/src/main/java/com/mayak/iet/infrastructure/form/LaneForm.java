package com.mayak.iet.infrastructure.form;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneTypeDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.infrastructure.common.ResetUtils;
import com.mayak.iet.infrastructure.assembler.LaneFormAssembler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.util.Map;

public class LaneForm {

    private final TextField laneId;
    private final TextField from;
    private final TextField to;
    private final TextField temp;
    private final TextField weight;
    private final TextField price;
    private final TextField fuel;

    private final ComboBox<ShipmentTypeDto> shipment;
    private final ComboBox<TransportTypeDto> transport;
    private final ComboBox<LaneTypeDto> laneType;

    private final DatePicker validFrom;
    private final DatePicker validTo;

    public LaneForm(
            TextField laneId,
            TextField from,
            TextField to,
            TextField temp,
            TextField weight,
            TextField price,
            TextField fuel,
            ComboBox<ShipmentTypeDto> shipment,
            ComboBox<TransportTypeDto> transport,
            ComboBox<LaneTypeDto> laneType,
            DatePicker validFrom,
            DatePicker validTo
    ) {
        this.laneId = laneId;
        this.from = from;
        this.to = to;
        this.temp = temp;
        this.weight = weight;
        this.price = price;
        this.fuel = fuel;
        this.shipment = shipment;
        this.transport = transport;
        this.laneType = laneType;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public LaneCreateDto toDto() {
        return LaneFormAssembler.buildLaneDto(
                laneId, from, to, temp, weight, price,
                fuel, shipment, transport, validFrom, validTo, laneType
        );
    }

    public Map<String, Control> fieldMap() {
        return Map.ofEntries(
                Map.entry("laneName", laneId),
                Map.entry("fromLocation", from),
                Map.entry("toLocation", to),
                Map.entry("temperature", temp),
                Map.entry("weight", weight),
                Map.entry("shipmentType", shipment),
                Map.entry("transportType", transport),
                Map.entry("price", price),
                Map.entry("fuelSurcharge", fuel),
                Map.entry("validFrom", validFrom),
                Map.entry("validTo", validTo),
                Map.entry("type", laneType)
        );
    }

    public void reset() {
        ResetUtils.reset(
                laneId, from, to, temp, weight, price,
                fuel, shipment, transport, validFrom, validTo, laneType
        );
    }
}
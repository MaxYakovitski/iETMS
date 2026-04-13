package com.mayak.ietms.ui.workspace.planner.presenter;

import com.mayak.ietms.domain.planner.model.ShipmentContext;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalTime;

public record TransportDetailsInput(
        ShipmentListItemDto dto,
        ShipmentContext ctx,
        Label shipmentNumber,
        VBox timelineContainer,
        VBox transportDetails,
        TextField carrierField,
        TextArea commentsTextArea,
        TextField licensePlateField,
        TextField transportOrder,
        ComboBox<ShipmentStatusDto> statusComboBox,
        DatePicker datePicker,
        Spinner<LocalTime> timeSpinner) {}
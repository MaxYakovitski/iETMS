package com.mayak.ietms.ui.workspace.planner.policy;

import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class PlannerStatusEditPolicy {

    private static final List<ShipmentStatusDto> ALL_EDITABLE_STATUSES =
            List.of(ShipmentStatusDto.LOADED, ShipmentStatusDto.DROPPED);

    public void configure(ComboBox<ShipmentStatusDto> comboBox) {
        comboBox.getItems().setAll(ALL_EDITABLE_STATUSES);
        comboBox.setDisable(true);
    }

    public void apply(ShipmentStatusDto selectedStatus, DatePicker datePicker, Spinner<LocalTime> timeSpinner) {
        boolean enabled = selectedStatus != null;

        datePicker.setDisable(!enabled);
        timeSpinner.setDisable(!enabled);

        if (enabled && timeSpinner.getValue() == null) {
            timeSpinner.getValueFactory().setValue(LocalTime.now());
        }
    }

    public void reset(DatePicker datePicker, Spinner<LocalTime> timeSpinner) {
        datePicker.setValue(null);
        timeSpinner.getValueFactory().setValue(null);
        datePicker.setDisable(true);
        timeSpinner.setDisable(true);
    }

    public void applyAllowedStatus(
            ComboBox<ShipmentStatusDto> comboBox,
            ShipmentStatusDto allowedStatus
    ) {
        if (allowedStatus == null) {
            comboBox.getItems().clear();
            comboBox.setDisable(true);
        } else {
            comboBox.getItems().setAll(allowedStatus);
            comboBox.setDisable(false);
        }
    }
}
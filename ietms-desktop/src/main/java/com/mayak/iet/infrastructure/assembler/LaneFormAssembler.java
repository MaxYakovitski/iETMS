package com.mayak.iet.infrastructure.assembler;

import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneTypeDto;
import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.location.util.LocationParser;
import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LaneFormAssembler {

    public static LaneCreateDto buildLaneDto(
            TextField laneIdField,
            TextField fromField,
            TextField toField,
            TextField tempTextField,
            TextField weightField,
            TextField priceField,
            TextField fuelField,
            ComboBox<ShipmentTypeDto> shipmentCombo,
            ComboBox<TransportTypeDto> transportCombo,
            DatePicker validFrom,
            DatePicker validTo,
            ComboBox<LaneTypeDto> laneTypeCombo
    ) {

        LocationDto from = parseLocation(fromField.getText());
        LocationDto to = parseLocation(toField.getText());

        Double weight = TextUtils.parseDoubleSafe(weightField.getText());
        String temperature = tempTextField.getText();

        BigDecimal price = TextUtils.parseBigDecimalSafe(priceField.getText());
        if (price != null) price = price.setScale(2, RoundingMode.HALF_UP);

        BigDecimal surcharge = TextUtils.parseBigDecimalSafe(fuelField != null ? fuelField.getText() : null);
        if (surcharge != null)
            surcharge = surcharge.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return new LaneCreateDto(
                TextUtils.safeTrim(laneIdField.getText()),
                from,
                to,
                temperature,
                weight,
                shipmentCombo.getValue(),
                transportCombo.getValue(),
                price,
                surcharge,
                validFrom.getValue(),
                validTo.getValue(),
                laneTypeCombo.getValue()
        );
    }

    private static LocationDto parseLocation(String text) {
        return LocationParser.parse(text);
    }
}

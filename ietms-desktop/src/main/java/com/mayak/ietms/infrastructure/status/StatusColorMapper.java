package com.mayak.ietms.infrastructure.status;

import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.scene.paint.Color;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StatusColorMapper {
    public static Color toColor(RequestStatusDto status) {
        if (status == null) {
            return TextUtils.SYSTEM_TEXT_DEFAULT_COLOR;
        }

        return switch (status) {
            case ACCEPTED -> TextUtils.SYSTEM_TEXT_GREEN_COLOR;
            case REFUSED -> TextUtils.SYSTEM_TEXT_RED_COLOR;
            default -> TextUtils.SYSTEM_TEXT_DEFAULT_COLOR;
        };
    }

    public static Color toColor(ShipmentStatusDto status) {
        if (status == null) {
            return TextUtils.SYSTEM_TEXT_DEFAULT_COLOR;
        }

        return switch (status) {
            case PLANNED, LOADED, DROPPED -> TextUtils.SYSTEM_TEXT_DEFAULT_COLOR;
            case CANCELED        -> TextUtils.SYSTEM_TEXT_RED_COLOR;
        };
    }
}
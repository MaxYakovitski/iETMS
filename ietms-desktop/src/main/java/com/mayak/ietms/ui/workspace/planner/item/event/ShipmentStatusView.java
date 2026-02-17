package com.mayak.ietms.ui.workspace.planner.item.event;

import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.scene.paint.Color;

public record ShipmentStatusView(
        String label,
        Color color) {

    public static ShipmentStatusView planned() {
        return new ShipmentStatusView("PLANNED", TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
    }

    public static ShipmentStatusView loaded() {
        return new ShipmentStatusView("LOADED", TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
    }

    public static ShipmentStatusView dropped() {
        return new ShipmentStatusView("DROPPED", TextUtils.SYSTEM_TEXT_GREEN_COLOR);
    }

    public static ShipmentStatusView canceled() {
        return new ShipmentStatusView("CANCELED", TextUtils.SYSTEM_TEXT_RED_COLOR);
    }
}
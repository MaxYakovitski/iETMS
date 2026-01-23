package com.mayak.iet.ui.workspace.planner.item.event;

import javafx.scene.paint.Color;

import java.time.LocalDateTime;

public record ShipmentLastEvent (
        String label,
        LocalDateTime at,
        Color color) {
}
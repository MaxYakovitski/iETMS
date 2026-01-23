package com.mayak.iet.infrastructure.ui;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LabelTooltipUtils {

    public static void attachAutoTooltip(Label... labels) {
        if (labels == null) return;

        for (Label label : labels) {
            if (label == null) continue;

            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(label.textProperty());
            label.setTooltip(tooltip);
        }
    }
}

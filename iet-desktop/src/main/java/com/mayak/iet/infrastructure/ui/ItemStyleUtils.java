package com.mayak.iet.infrastructure.ui;

import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.scene.control.Label;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ItemStyleUtils {

    public static void applyDefaultTextColor(Label... labels) {
        for (Label label : labels) {
            if (label != null) {
                label.setTextFill(TextUtils.SYSTEM_TEXT_DEFAULT_COLOR);
            }
        }
    }
}

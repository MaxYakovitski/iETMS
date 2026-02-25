package com.mayak.ietms.infrastructure.ui;

import javafx.scene.control.TextField;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ControlSizingUtils {

    public static void fitTextFieldToDigits(TextField tf) {
        tf.textProperty().addListener((obs, o, n) -> {
            int len = (n == null || n.isEmpty()) ? 1 : n.length();
            tf.setPrefColumnCount(len);
        });

        String t = tf.getText();
        tf.setPrefColumnCount((t == null || t.isEmpty()) ? 1 : t.length());
    }
}
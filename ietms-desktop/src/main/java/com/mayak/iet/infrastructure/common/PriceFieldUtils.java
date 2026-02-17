package com.mayak.iet.infrastructure.common;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public class PriceFieldUtils {
    private static final BigDecimal MAX_VALUE = new BigDecimal("99999");

    public static void setupPriceField(TextField field) {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String input = change.getControlNewText().replace(",", ".");
            if (!input.matches(TextUtils.PRICE_PATTEN)) return null;

            if (!input.isEmpty()) {
                try {
                    BigDecimal value = new BigDecimal(input);
                    if (value.compareTo(MAX_VALUE) > 0) {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            return change;
        };

        field.setTextFormatter(new TextFormatter<>(filter));
        field.getStyleClass().add("price-field");

        field.textProperty().addListener((val, oldVal, newVal) -> ErrorUtils.removeErrorStyle(field));
        field.focusedProperty().addListener((val, oldVal, newVAl) -> ErrorUtils.removeErrorStyle(field));
    }

    public static BigDecimal parseOrNull(TextField field) {
        return TextUtils.parseBigDecimalSafe(field.getText());
    }

    public static boolean validate(TextField field) {
        if (parseOrNull(field) == null) {
            ErrorUtils.addErrorStyle(field);
            return false;
        }
        return true;
    }
}
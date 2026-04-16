package com.mayak.ietms.infrastructure.common;

import com.mayak.ietms.common.util.TextSanitizer;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class TextUtils {

    public static final Color SYSTEM_TEXT_DEFAULT_COLOR = Color.valueOf("#313131");
    public static final Color SYSTEM_TEXT_RED_COLOR = Color.valueOf("#e74c3c");
    public static final Color SYSTEM_TEXT_GREEN_COLOR = Color.valueOf("#4caf50");

    public static final String DATE_DOTS_PROMPT = "dd.mm.yyyy";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm • dd-MM-yyyy");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATE_DOTS_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter MONTH_STANDALONE = DateTimeFormatter.ofPattern("LLLL");

    public static final String PRICE_PATTEN = "\\d*([.,]\\d*)?";

    public static void allowOnlyLatin(TextInputControl... controls) {
        String regex = "[ -~]+";
        UnaryOperator<TextFormatter.Change> latinFilter = change -> {
            String inserted = change.getText();
            if (inserted == null || inserted.isEmpty()) return change;
            return inserted.matches(regex) ? change : null;
        };
        for (TextInputControl control : controls) {
            control.setTextFormatter(new TextFormatter<>(latinFilter));
        }
    }

    public static String formatMoney(BigDecimal value) {
        return value != null ? value.toPlainString() + " EUR" : "";}

    public static String formatWeight(Double weight) {
        return weight != null ? String.format("%.2f kg", weight) : "";
    }

    public static String formatLoadingMeters(Double ldm) {
        return ldm != null ? String.format("%.2f ldm", ldm) : "";
    }

    public static String formatTemperature(String temperature) {
        if (temperature == null || temperature.isBlank()) {
            return "";
        }
        return temperature.replaceFirst("\\s+", "...") + "°C";
    }

    public static Double parseDoubleSafe(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Double.parseDouble(text.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String safeTrim(String text) {
        return TextSanitizer.safeTrim(text);
    }

    public static BigDecimal parseBigDecimalSafe(String text) {
        try {
            return (text == null || text.isBlank()) ? null : new BigDecimal(text.replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }

    public static <E> void setupEnumComboBox(ComboBox<E> comboBox, E[] values, Function<E, String> labelProvider) {
        comboBox.getItems().setAll(values);

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(E value) {
                return value == null ? "" : labelProvider.apply(value);
            }

            @Override
            public E fromString(String label) {
                return Arrays.stream(values)
                        .filter(v -> labelProvider.apply(v).equals(label))
                        .findFirst()
                        .orElse(null);
            }
        });

        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelProvider.apply(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : labelProvider.apply(item));
            }
        });
    }
}
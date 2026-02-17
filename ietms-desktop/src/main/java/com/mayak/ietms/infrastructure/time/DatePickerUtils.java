package com.mayak.ietms.infrastructure.time;

import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.function.Supplier;

public class DatePickerUtils {

    public static void setupDatePickers(
            DatePicker start,
            DatePicker end
    ) {
        setupDatePickers(start, end, () -> null, () -> null);
    }

    public static void setupDatePickers(
            DatePicker start,
            DatePicker end,
            Supplier<LocalDate> minSupplier
    ) {
        setupDatePickers(start, end, minSupplier, () -> null);
    }

    public static void setupDatePickers(
            DatePicker start,
            DatePicker end,
            Supplier<LocalDate> minSupplier,
            Supplier<LocalDate> maxSupplier
    ) {
        setupBase(start);
        setupBase(end);

        start.setDayCellFactory(cell -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;

                LocalDate min = minSupplier.get();
                LocalDate max = maxSupplier.get();
                LocalDate endValue = end.getValue();

                boolean invalid =
                        (min != null && item.isBefore(min)) ||
                                (max != null && item.isAfter(max)) ||
                                (endValue != null && item.isAfter(endValue));

                if (invalid) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f8d7da;");
                }
            }
        });

        end.setDayCellFactory(cell -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;

                LocalDate min = minSupplier.get();
                LocalDate max = maxSupplier.get();
                LocalDate startValue = start.getValue();

                boolean invalid =
                        (min != null && item.isBefore(min)) ||
                                (max != null && item.isAfter(max)) ||
                                (startValue != null && item.isBefore(startValue));

                if (invalid) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f8d7da;");
                }
            }
        });
    }

    public static void setupDatePicker(DatePicker picker) {
        setupBase(picker);
    }

    private static void setupBase(DatePicker picker) {

        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? TextUtils.DATE_DOTS_FORMATTER.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string == null || string.isBlank())
                        ? null
                        : LocalDate.parse(string, TextUtils.DATE_DOTS_FORMATTER);
            }
        });

        picker.setPromptText(TextUtils.DATE_DOTS_PROMPT);
    }
}

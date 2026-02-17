package com.mayak.iet.infrastructure.time;

import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@NoArgsConstructor
public class TimeSpinnerUtils {

    public static void setupTimeSpinner(Spinner<LocalTime> spinner, int stepMinutes) {

        SpinnerValueFactory<LocalTime> factory =
                new SpinnerValueFactory<>() {

                    @Override
                    public void increment(int steps) {
                        if (getValue() == null) return;
                        setValue(getValue().plusMinutes((long) stepMinutes * steps));
                    }

                    @Override
                    public void decrement(int steps) {
                        if (getValue() == null) return;
                        setValue(getValue().minusMinutes((long) stepMinutes * steps));
                    }
                };

        factory.setValue(null);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);

        factory.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime time) {
                return time == null ? "" : TextUtils.TIME_FORMATTER.format(time);
            }

            @Override
            public LocalTime fromString(String text) {
                if (text == null || text.isBlank()) {
                    return factory.getValue();
                }
                try {
                    return LocalTime.parse(text.trim(), TextUtils.TIME_FORMATTER);
                } catch (Exception e) {
                    return factory.getValue();
                }
            }
        });

        spinner.getEditor().focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                LocalTime value = spinner.getValue();
                spinner.getEditor().setText(
                        value != null ? TextUtils.TIME_FORMATTER.format(value) : ""
                );
            }
        });
    }

}

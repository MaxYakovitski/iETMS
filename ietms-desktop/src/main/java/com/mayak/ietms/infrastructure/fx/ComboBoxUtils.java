package com.mayak.ietms.infrastructure.fx;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import java.util.function.Function;

public class ComboBoxUtils {
    public static <T> void setupPrompt(ComboBox<T> combo, Function<T, String> toStringFunc) {

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? combo.getPromptText()
                        : toStringFunc.apply(item));
            }
        });

        combo.setCellFactory(item -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : toStringFunc.apply(item));
            }
        });
    }
}
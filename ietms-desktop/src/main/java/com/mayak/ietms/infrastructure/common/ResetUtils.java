package com.mayak.ietms.infrastructure.common;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import org.controlsfx.control.CheckComboBox;

public class ResetUtils {
    public static  <T> void resetOnChange(Control control, ObservableValue<T> property) {
        property.addListener((value, oldValue, newValue) -> {
            if (newValue != null && !(newValue instanceof String && ((String) newValue).isEmpty())) {
                ErrorUtils.removeErrorStyle(control);
            }
        });

        control.disableProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                ErrorUtils.removeErrorStyle(control);
            }
        });
    }

    public static void resetOnCheckChange(CheckComboBox<?> checkComboBox) {
        checkComboBox.getCheckModel()
                .getCheckedItems()
                .addListener((ListChangeListener<Object>) change -> {
                    if (!checkComboBox.getCheckModel().getCheckedItems().isEmpty()) {
                        ErrorUtils.removeErrorStyle(checkComboBox);
                    }
                });
        checkComboBox.disableProperty().addListener((value, oldValue, newValue) -> {
            if (newValue) {
                ErrorUtils.removeErrorStyle(checkComboBox);
            }
        });
    }

    public static void reset(Object... components) {
        for (Object obj : components) {
            if (obj instanceof TextField field) {
                field.clear();
            } else if (obj instanceof ComboBox<?> box) {
                box.getSelectionModel().clearSelection();
            } else if (obj instanceof ChoiceBox<?> choiceBox) {
                choiceBox.getSelectionModel().clearSelection();
            } else if (obj instanceof CheckBox checkBox) {
                checkBox.setSelected(false);
            } else if (obj instanceof TextArea textArea) {
                textArea.clear();
            } else if (obj instanceof ToggleGroup group) {
                group.selectToggle(null);
            } else if (obj instanceof DatePicker datePicker) {
                datePicker.setValue(null);
            } else if (obj instanceof Control control) {
                ErrorUtils.removeErrorStyle(control);
            }
        }
    }
}
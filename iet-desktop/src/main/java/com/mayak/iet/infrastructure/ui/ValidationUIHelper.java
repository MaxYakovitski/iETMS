package com.mayak.iet.infrastructure.ui;

import com.mayak.iet.integration.exception.ApiValidationException;
import com.mayak.iet.infrastructure.common.ErrorUtils;
import com.mayak.iet.infrastructure.common.ResetUtils;
import com.mayak.iet.common.validation.ValidationError;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public record ValidationUIHelper(Map<String, Control> fieldMap) {

    public void showBackendErrors(ApiValidationException exception) {
        fieldMap.values().forEach(control -> {
            ErrorUtils.removeErrorStyle(control);
            Tooltip.uninstall(control, null);
        });

        var errors = exception.getErrors();
        if (errors.isEmpty()) return;

        errors.forEach((field, message) -> {
            Control control = fieldMap.get(field);
            if (control != null) {
                ErrorUtils.addErrorStyle(control);

                Tooltip tooltip = new Tooltip(message);
                tooltip.setWrapText(true);
                tooltip.setMaxWidth(300);

                Tooltip.install(control, tooltip);
            }
        });
    }

    public void showClientErrors(List<ValidationError> errors) {
        fieldMap.values().forEach(ErrorUtils::removeErrorStyle);

        if (errors == null || errors.isEmpty()) {
            return;
        }

        errors.forEach(error -> {
            Control control = fieldMap.get(error.code());
            if (control != null) {
                ErrorUtils.addErrorStyle(control);
            }
        });
    }

    public void bindResetOnChange() {
        fieldMap.values().forEach(control -> {

            if (control instanceof TextInputControl text) {
                ResetUtils.resetOnChange(text, text.textProperty());
            }

            if (control instanceof ComboBox<?> comboBox) {
                ResetUtils.resetOnChange(comboBox, comboBox.valueProperty());
            }

            if (control instanceof DatePicker datePicker) {
                ResetUtils.resetOnChange(datePicker, datePicker.valueProperty());
            }

            if (control instanceof RadioButton radioButton) {
                ToggleGroup group = radioButton.getToggleGroup();
                if (group != null) {
                    group.selectedToggleProperty().addListener((obs, old, selected) -> {
                        if (selected != null) {
                            ErrorUtils.removeErrorStyle((RadioButton) selected);
                        }
                    });
                }
            }

        });
    }

    public void clearError(String fieldKey) {
        Control control = fieldMap.get(fieldKey);
        if (control != null) {
            clearError(control);
        }
    }

    public void clearError(Control control) {
        ErrorUtils.removeErrorStyle(control);
        Tooltip.uninstall(control, null);
    }
}
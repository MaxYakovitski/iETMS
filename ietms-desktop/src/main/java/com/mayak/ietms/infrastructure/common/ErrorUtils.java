package com.mayak.ietms.infrastructure.common;

import javafx.scene.Node;
import javafx.scene.control.*;
import org.controlsfx.control.CheckComboBox;

public class ErrorUtils {
    private static final String UNIVERSAL_ERROR = "text-field-error";
    private static final String PRICE_ERROR = "price-field-error";
    private static final String RADIO_ERROR = "radio-error";
    private static final String DATE_PIKER_ERROR = "date-picker-error";
    private static final String COMBO_BOX_ERROR = "combo-error";
    private static final String CHECK_COMBO_BOX_ERROR = "combo-error";

    public static void addErrorStyle(Node field) {
        String errorClass = getErrorClass(field);

        if (field instanceof CheckComboBox<?> checkCombo) {
            if (!checkCombo.getStyleClass().contains(CHECK_COMBO_BOX_ERROR)) {
                checkCombo.getStyleClass().add(CHECK_COMBO_BOX_ERROR);
            }
            return;
        }

        if (field instanceof ComboBox<?> comboBox) {
            comboBox.getStyleClass().add(COMBO_BOX_ERROR);
            return;
        }

        switch (field) {
            case DatePicker datePicker -> {
                if (!datePicker.getStyleClass().contains(DATE_PIKER_ERROR)) {
                    datePicker.getStyleClass().add(DATE_PIKER_ERROR);
                }
            }
            case RadioButton radioButton -> {
                ToggleGroup group = radioButton.getToggleGroup();
                if (group != null) {
                    for (Toggle toggle : group.getToggles()) {
                        if (toggle instanceof RadioButton rb) {
                            rb.getStyleClass().add(RADIO_ERROR);
                        }
                    }
                }
            }
            case Control control -> control.getStyleClass().add(errorClass);
            default -> {
            }
        }
    }

    public static void removeErrorStyle(Node field) {
        if (field instanceof CheckComboBox<?> checkCombo) {
            checkCombo.getStyleClass().remove(CHECK_COMBO_BOX_ERROR);
            return;
        }

        if (field instanceof ComboBox<?> comboBox) {
            comboBox.getStyleClass().remove(COMBO_BOX_ERROR);
            return;
        }

        String errorClass = getErrorClass(field);

        switch (field) {
            case DatePicker datePicker -> datePicker.getStyleClass().remove(DATE_PIKER_ERROR);

            case RadioButton radioButton -> {
                ToggleGroup group = radioButton.getToggleGroup();
                if (group != null) {
                    for (Toggle toggle : group.getToggles()) {
                        if (toggle instanceof RadioButton rb) {
                            rb.getStyleClass().remove(RADIO_ERROR);
                        }
                    }
                } else {
                    radioButton.getStyleClass().remove(RADIO_ERROR);
                }
            }
            case Control control -> control.getStyleClass().remove(errorClass);
            default -> {}
        }
    }

    private static String getErrorClass(Node field) {
        if (field.getStyleClass().contains("price-field")) {
            return PRICE_ERROR;
        } else if (field instanceof DatePicker) {
            return DATE_PIKER_ERROR;
        } else {
            return UNIVERSAL_ERROR;
        }
    }
}
package com.mayak.ietms.support.validation;

import javafx.scene.control.DatePicker;

public record DateRange(
        DatePicker startPicker,
        DatePicker endPicker) {
}
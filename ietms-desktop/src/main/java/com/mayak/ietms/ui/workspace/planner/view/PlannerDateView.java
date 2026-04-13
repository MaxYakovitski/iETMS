package com.mayak.ietms.ui.workspace.planner.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class PlannerDateView extends VBox {

    public PlannerDateView() {
        setAlignment(Pos.CENTER);
        setSpacing(5);

        LocalDate today = LocalDate.now();

        Label dayNumberLabel = new Label(String.valueOf(today.getDayOfMonth()));
        dayNumberLabel.setMinSize(48, 48);
        dayNumberLabel.setMaxSize(48, 48);
        dayNumberLabel.setAlignment(Pos.CENTER);
        dayNumberLabel.getStyleClass().add("calendar-day--selected");
        dayNumberLabel.setStyle("-fx-font-size: 24;");

        Label monthYearLabel = new Label(today.getMonth().getDisplayName(TextStyle.FULL, Locale.UK) + ", " + today.getYear());

        int week = today.get(WeekFields.of(Locale.UK).weekOfWeekBasedYear());
        String day = today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.UK).toUpperCase();
        Label weekDayLabel = new Label("Week: " + week + ", " + day);

        getChildren().addAll(dayNumberLabel, monthYearLabel, weekDayLabel);
    }
}
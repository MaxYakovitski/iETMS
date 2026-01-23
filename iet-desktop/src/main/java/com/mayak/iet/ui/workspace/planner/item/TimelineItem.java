package com.mayak.iet.ui.workspace.planner.item;

import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;

public class TimelineItem extends HBox {

    private final Circle dot = new Circle(5);
    private final Region line = new Region();
    private final Label statusLabel = new Label();
    private final Label dateLabel = new Label();

    public TimelineItem(String status, LocalDateTime dateTime, Color color, boolean isLast) {

        setSpacing(10);
        setAlignment(Pos.CENTER);

        dot.setFill(color);

        line.setMaxWidth(2);
        line.setPrefHeight(40);
        line.getStyleClass().add("timeline-line");

        if (isLast) {
            line.setVisible(false);
            line.setManaged(false);
        }

        statusLabel.setText(status.toUpperCase());
        statusLabel.getStyleClass().add("timeline-status");

        dateLabel.setText(dateTime != null ? TextUtils.DATE_TIME_FORMATTER.format(dateTime) : "");
        dateLabel.getStyleClass().add("timeline-date");

        VBox left = new VBox(dot, line);
        left.setAlignment(Pos.TOP_CENTER);

        VBox right = new VBox(statusLabel, dateLabel);
        right.setSpacing(2);
        right.setMinWidth(140);
        right.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(left, right);
    }
}

package com.mayak.ietms.ui.workspace.planner.item;

import com.mayak.ietms.infrastructure.common.TextUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.time.LocalDateTime;

public class TimelineItem extends VBox {

    private final Label statusLabel = new Label();
    private final Label dateLabel = new Label();
    private final Circle topDot = new Circle(5);
    private final Circle bottomDot = new Circle(5);
    private final Region line = new Region();

    public TimelineItem(String status, LocalDateTime dateTime, Color color, boolean showTopDot, boolean showBottomDot, boolean showLine) {

        setAlignment(Pos.CENTER);

        statusLabel.setText(status.toUpperCase());
        statusLabel.getStyleClass().add("timeline-status");
        dateLabel.setText(dateTime != null ? TextUtils.DATE_TIME_FORMATTER.format(dateTime) : "");
        dateLabel.getStyleClass().add("timeline-date");

        VBox textBox = new VBox(statusLabel, dateLabel);
        textBox.setSpacing(0);
        textBox.setAlignment(Pos.CENTER);

        VBox.setMargin(textBox, new Insets(0, 0, 5, 0));

        topDot.setFill(color);
        topDot.setVisible(showTopDot);
        topDot.setManaged(showTopDot);

        bottomDot.setFill(color);
        bottomDot.setVisible(showBottomDot);
        bottomDot.setManaged(showBottomDot);

        line.setMaxWidth(2);
        line.setPrefHeight(40);
        line.getStyleClass().add("timeline-line");

        line.setVisible(showLine);
        line.setManaged(showLine);

        if (showTopDot) getChildren().add(topDot);
        getChildren().add(textBox);
        if (showBottomDot) getChildren().add(bottomDot);
        if (showLine) getChildren().add(line);

    }
}

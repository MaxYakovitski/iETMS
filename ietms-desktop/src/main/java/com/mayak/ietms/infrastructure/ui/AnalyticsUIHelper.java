package com.mayak.ietms.infrastructure.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class AnalyticsUIHelper {

    public static Label headerLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold;");
        return lbl;
    }

    public static VBox createReportBox(String title, GridPane grid) {
        VBox box = new VBox(5);
        box.setBackground(new Background(
                new BackgroundFill(Color.web("#f9f9f9"), new CornerRadii(5), Insets.EMPTY)
        ));
        box.setPadding(new Insets(10, 10, 10, 10));
        VBox.setMargin(box, new Insets(0, 0, 25, 0));

        Label titleLabel = headerLabel(title);

        box.getChildren().addAll(titleLabel, grid);
        return box;
    }

    public static GridPane createDefaultGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(50);
        grid.setVgap(5);
        return grid;
    }

    public static void addHeaderSeparator(GridPane grid, int columnCount, int rowIndex) {
        Separator sep = new Separator();
        GridPane.setColumnSpan(sep, columnCount);
        grid.add(sep, 0, rowIndex);
    }
}
package com.mayak.ietms.ui.analytics.view;

import com.mayak.ietms.ui.analytics.model.UserReport;
import com.mayak.ietms.ui.analytics.model.UserReportItem;
import com.mayak.ietms.infrastructure.ui.AnalyticsUIHelper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Component;

@Component
public class UserReportRenderer {

    public VBox render(UserReport report) {
        GridPane grid = AnalyticsUIHelper.createDefaultGrid();
        addHeader(grid);

        int row = 2;
        for (UserReportItem item : report.items()) {
            addRow(grid, row++, item);
        }

        VBox box = new VBox(5);
        box.setBackground(new Background(
                new BackgroundFill(Color.web("#f9f9f9"), new CornerRadii(5), Insets.EMPTY)
        ));
        box.setPadding(new Insets(10));
        VBox.setMargin(box, new Insets(0, 0, 25, 0));

        Label title = new Label(report.user().name().fullName());
        title.setStyle("-fx-font-weight: bold;");

        box.getChildren().addAll(title, grid);
        return box;
    }

    private void addHeader(GridPane grid) {
        grid.add(new Label("placed"), 0, 0);
        grid.add(new Label("joined"), 1, 0);
        grid.add(new Label("bided"), 2, 0);
        grid.add(new Label("accepted spot/contract"), 3, 0);
        grid.add(new Label("dispatched"), 4, 0);
        grid.add(new Label("spot avg.bid time(min)"), 5, 0);

        grid.add(new Separator(), 0, 1, 6, 1);
    }

    private void addRow(GridPane grid, int row, UserReportItem item) {
        grid.add(new Label(String.valueOf(item.placed())), 0, row);
        grid.add(new Label(String.valueOf(item.joined())), 1, row);
        grid.add(new Label(String.valueOf(item.bided())), 2, row);
        grid.add(new Label(item.acceptedSpot() + "/" + item.acceptedContract()), 3, row);
        grid.add(new Label(String.valueOf(item.dispatched())), 4, row);
        grid.add(new Label(item.avgResponse().toString()), 5, row);
    }
}
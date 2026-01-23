package com.mayak.iet.ui.analytics.view;

import com.mayak.iet.infrastructure.ui.AnalyticsUIHelper;
import com.mayak.iet.ui.analytics.model.CompanyReport;
import com.mayak.iet.ui.analytics.model.CompanyReportItem;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CompanyReportRenderer {

    public VBox render(CompanyReport report) {
        GridPane grid = AnalyticsUIHelper.createDefaultGrid();
        addHeader(grid);

        int row = 2;
        for (CompanyReportItem item : report.items()) {
            addRow(grid, row++, item);
        }

        return AnalyticsUIHelper.createReportBox(report.company().name(), grid);
    }

    private void addHeader(GridPane grid) {
        grid.add(new Label("Lane"), 0, 0);
        grid.add(new Label("Transport"), 1, 0);
        grid.add(new Label("Spot:"), 2, 0);
        grid.add(new Label("Efficiency:"), 3, 0);
        grid.add(new Label("Profit margin, EUR"), 4, 0);
        grid.add(new Label("Contract:"), 5, 0);
        grid.add(new Label("Efficiency:"), 6, 0);
        grid.add(new Label("Profit margin, EUR"), 7, 0);
        AnalyticsUIHelper.addHeaderSeparator(grid, 8, 1);
    }

    private void addRow(GridPane grid, int row, CompanyReportItem item) {
        grid.add(new Label(item.lane()), 0, row);
        grid.add(new Label(item.transportType()), 1, row);

        grid.add(new Label(String.valueOf(item.spot())), 2, row);
        grid.add(new Label(formatPercent(item.spotEfficiency())), 3, row);
        grid.add(new Label(formatMoney(item.spotMargin())), 4, row);

        grid.add(new Label(String.valueOf(item.contract())), 5, row);
        grid.add(new Label(formatPercent(item.contractEfficiency())), 6, row);
        grid.add(new Label(formatMoney(item.contractMargin())), 7, row);
    }

    private String formatPercent(double value) {
        return String.format("%.2f %%", value);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }
}

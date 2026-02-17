package com.mayak.iet.ui.analytics.view;

import com.mayak.iet.statistics.MonthlyCountDto;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DepartmentChartsRenderer {

    public void renderPie(
            int a,
            int b,
            String labelA,
            String labelB,
            PieChart chart,
            StackPane container
    ) {
        int total = a + b;

        if (total == 0) {
            showPlaceholder(container);
            chart.setVisible(false);
            return;
        }

        chart.setVisible(true);
        container.getChildren().setAll(chart);

        PieChart.Data d1 = new PieChart.Data(labelA + " (" + percent(a, total) + "%)\n" + a, a);
        PieChart.Data d2 = new PieChart.Data(labelB + " (" + percent(b, total) + "%)\n" + b, b);

        chart.getData().setAll(d1, d2);
        chart.setLabelsVisible(false);
        animate(chart);
    }

    public void renderBar(
            Map<String, Integer> data,
            BarChart<Number, String> chart
    ) {
        chart.getData().clear();

        CategoryAxis yAxis = (CategoryAxis) chart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setCategories(FXCollections.observableArrayList(data.keySet()));

        String[] colors = {
                "#e96c3f",
                "#e3971c",
                "#4ca8c4",
                "#d94f42",
                "#2990ff",
                "#5ab05a"
        };

        BarChart.Series<Number, String> series = new BarChart.Series<>();

        int index = 0;
        for (var entry : data.entrySet()) {
            BarChart.Data<Number, String> d =
                    new BarChart.Data<>(entry.getValue(), entry.getKey());

            final int colorIndex = index;
            d.nodeProperty().addListener((node, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-bar-fill: " + colors[colorIndex % colors.length] + ";");
                }
            });

            series.getData().add(d);
            index++;
        }

        chart.getData().add(series);

        Platform.runLater(() -> {
            chart.applyCss();
            chart.layout();
            yAxis.setAutoRanging(false);
        });
    }

    private void animate(PieChart chart) {
        for (PieChart.Data d : chart.getData()) {

            double target = d.getPieValue();

            DoubleProperty p = new SimpleDoubleProperty(0);
            d.pieValueProperty().bind(p);

            Timeline t = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(p, 0)),
                    new KeyFrame(Duration.seconds(1), new KeyValue(p, target))
            );
            t.play();
        }
    }

    private void showPlaceholder(StackPane container) {
        Label lbl = new Label("No data for this field...");
        lbl.getStyleClass().add("chart-placeholder");
        container.getChildren().setAll(lbl);
    }

    private String percent(int v, int total) {
        return String.format("%.2f", (v * 100.0) / total);
    }

    public void renderCompression(List<MonthlyCountDto> data, LineChart<String, Number> chart) {
        chart.getData().clear();
        chart.setAnimated(false);

        var spotSeries = new LineChart.Series<String, Number>();
        spotSeries.setName("Spot");

        var contractSeries = new LineChart.Series<String, Number>();
        contractSeries.setName("Contract");

        for (var m : data) {
            spotSeries.getData().add(
                    new LineChart.Data<>(m.month(), m.spot())
            );
            contractSeries.getData().add(
                    new LineChart.Data<>(m.month(), m.contract())
            );
        }

        chart.getData().add(spotSeries);
        chart.getData().add(contractSeries);
    }
}
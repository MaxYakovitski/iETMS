package com.mayak.ietms.ui.analytics.view;

import com.mayak.ietms.statistics.MonthlyCountDto;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DepartmentChartsRenderer {

    public static final String COLOR_COMMON = "#306eed";
    public static final String COLOR_EFFICIENCY = "#81c784";

    public void renderProgressRing(int a, int b, String labelA, String labelB, String strokeColor, StackPane container) {
        int total = a + b;
        if (total == 0) {
            showPlaceholder(container);
            return;
        }

        container.getChildren().clear();

        double percent = (double) a / total;

        double size = 260;
        double strokeWidth = 28;

        // TRACK
        Circle track = new Circle(size / 2);
        track.setStroke(Color.web("#eeeeee"));
        track.setStrokeWidth(strokeWidth);
        track.setFill(null);

        // PROGRESS
        Arc progress = new Arc();
        progress.setManaged(false);
        progress.setMouseTransparent(true);

        progress.setStartAngle(90);
        progress.setLength(0);
        progress.setType(ArcType.OPEN);

        progress.setStrokeWidth(strokeWidth);
        progress.setStrokeLineCap(StrokeLineCap.ROUND);
        progress.setStroke(Paint.valueOf(strokeColor));
        progress.setFill(null);

        progress.setRadiusX(size / 2);
        progress.setRadiusY(size / 2);

        progress.centerXProperty().bind(container.widthProperty().divide(2));
        progress.centerYProperty().bind(container.heightProperty().divide(2));

        // CENTER LABEL
        Label center = new Label(String.format("%.2f%% %s", percent * 100, labelA));
        center.setAlignment(Pos.CENTER);

        container.getChildren().addAll(track, progress, center);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progress.lengthProperty(), 0)),
                new KeyFrame(Duration.seconds(1.4),
                        new KeyValue(progress.lengthProperty(),
                                -360 * percent,
                                Interpolator.EASE_BOTH))
        );

        timeline.play();
    }

    public void renderBar(Map<String, Integer> data, BarChart<Number, String> chart, StackPane container) {
        if (toggleChartPlaceholder(data, chart, container)) return;

        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);

        chart.getData().clear();

        CategoryAxis yAxis = (CategoryAxis) chart.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setCategories(FXCollections.observableArrayList(data.keySet()));

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setMinorTickVisible(false);

        int max = data.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);
        xAxis.setUpperBound(max);

        double tickUnit = niceTick(max / 5.0);
        xAxis.setTickUnit(tickUnit);


        BarChart.Series<Number, String> series = new BarChart.Series<>();
        String[] colors = {"#ed7647", "#81c784", "#306eed", "#e74c3c"};

        int index = 0;
        for (var entry : data.entrySet()) {
            int targetValue = entry.getValue();

            BarChart.Data<Number, String> d = new BarChart.Data<>(targetValue, entry.getKey());
            final int colorIndex = index;

            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("""
                    -fx-bar-fill: %s;
                    -fx-background-radius: 0 10 10 0;
                    -fx-background-insets: 0;
                    """.formatted(colors[colorIndex % colors.length]));

                }
            });

            series.getData().add(d);
            index++;
        }

        chart.setOpacity(0);
        chart.getData().add(series);

        Platform.runLater(() -> {
            chart.applyCss();
            chart.layout();

            for (BarChart.Data<Number, String> d : series.getData()) {

                Node node = d.getNode();
                if (node == null) continue;

                double width = node.getBoundsInLocal().getWidth();
                double height = node.getBoundsInLocal().getHeight();

                Rectangle clip = new Rectangle(0, height);
                node.setClip(clip);

                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.seconds(1.4),
                                new KeyValue(clip.widthProperty(), width, Interpolator.EASE_BOTH)));

                timeline.play();
            }

            chart.setOpacity(1);
        });
    }

    private boolean toggleChartPlaceholder(Map<String, Integer> data, BarChart<Number, String> chart, StackPane container) {
        if (data == null || data.isEmpty()) {
            chart.setManaged(false);
            chart.setVisible(false);
            container.setManaged(true);
            container.setVisible(true);
            showPlaceholder(container);
            return true;
        } else {
            container.setManaged(false);
            container.setVisible(false);

            chart.setManaged(true);
            chart.setVisible(true);
        }
        return false;
    }

    private double niceTick(double max) {
        double exponent = Math.floor(Math.log10(max));
        double fraction = max / Math.pow(10, exponent);

        double niceFraction;

        if (fraction <= 1) niceFraction = 1;
        else if (fraction <= 2) niceFraction = 2;
        else if (fraction <= 5) niceFraction = 5;
        else niceFraction = 10;

        return niceFraction * Math.pow(10, exponent);
    }

    private void showPlaceholder(StackPane container) {
        Label lbl = new Label("No data for this field...");
        lbl.getStyleClass().add("chart-placeholder");
        container.getChildren().setAll(lbl);
    }

    public void renderCompression(List<MonthlyCountDto> data, MonthlyCountDto currentMonth, LineChart<String, Number> chart) {
        chart.getData().clear();
        chart.setAnimated(false);

        var spotSeries = new LineChart.Series<String, Number>();
        spotSeries.setName("Spot");

        var contractSeries = new LineChart.Series<String, Number>();
        contractSeries.setName("Contract");

        for (var m : data) {
            spotSeries.getData().add(new LineChart.Data<>(m.month(), m.spot()));
            contractSeries.getData().add(new LineChart.Data<>(m.month(), m.contract()));
        }

        var currentSpotSeries = new LineChart.Series<String, Number>();
        currentSpotSeries.setName("Spot " + currentMonth.month());

        var currentContractSeries = new LineChart.Series<String, Number>();
        currentContractSeries.setName("Contract " + currentMonth.month());

        for (var m : data) {
            currentSpotSeries.getData().add(new LineChart.Data<>(m.month(), currentMonth.spot()));
            currentContractSeries.getData().add(new LineChart.Data<>(m.month(), currentMonth.contract()));
        }

        chart.getData().add(spotSeries);
        chart.getData().add(contractSeries);
        chart.getData().add(currentSpotSeries);
        chart.getData().add(currentContractSeries);

        Platform.runLater(() -> {
            applyCurrentMonthLineStyle(currentSpotSeries);
            applyCurrentMonthLineStyle(currentContractSeries);
        });
    }

    private void applyCurrentMonthLineStyle(LineChart.Series<String, Number> series) {
        Node line = series.getNode();
        if (line != null) {
            line.setStyle("-fx-stroke-width: 2; -fx-opacity: 0.5;");
        }
        for (var d : series.getData()) {
            Node symbol = d.getNode();
            if (symbol != null) {
                symbol.setVisible(false);
                symbol.setManaged(false);
            }
        }
    }
}
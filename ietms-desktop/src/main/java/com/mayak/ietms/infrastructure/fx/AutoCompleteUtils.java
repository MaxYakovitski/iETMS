package com.mayak.ietms.infrastructure.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@NoArgsConstructor
public class AutoCompleteUtils {

    private static final int MAX_HEIGHT = 150;
    private static final int CELL_HEIGHT = 28;

    public static void setupAutoCompletion(TextField field, Set<String> suggestions) {

        ListView<String> listView = new ListView<>();
        listView.getStyleClass().add("autocomplete-list");
        listView.setMaxHeight(MAX_HEIGHT);
        listView.setFocusTraversable(true);

        listView.getStylesheets().add(
                Objects.requireNonNull(
                        AutoCompleteUtils.class.getResource("/css/styles.css")
                ).toExternalForm()
        );

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(listView);

        final boolean[] ignoreTextChange = {false};

        // --- TEXT CHANGE ---
        field.textProperty().addListener((obs, oldVal, newVal) -> {

            if (!field.isFocused()) {
                popup.hide();
                return;
            }

            if (ignoreTextChange[0]) return;

            if (newVal == null || newVal.isBlank()) {
                popup.hide();
                return;
            }

            String query = newVal.toLowerCase();

            List<String> filtered = suggestions.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .filter(s -> s.toLowerCase().startsWith(query))
                    .toList();

            if (filtered.isEmpty()) {
                popup.hide();
                return;
            }

            listView.setItems(FXCollections.observableArrayList(filtered));
            listView.setPrefHeight(Math.min(filtered.size() * CELL_HEIGHT, MAX_HEIGHT));
            listView.setPrefWidth(field.getWidth());

            if (!popup.isShowing()) {
                showPopup(field, popup);
            }
        });

        // --- MOUSE SELECTION ---
        listView.setOnMouseClicked(e -> applySelection(field, listView, popup, ignoreTextChange));

        // --- KEYBOARD (FROM FIELD) ---
        field.setOnKeyPressed(e -> {

            if (!popup.isShowing()) return;

            if (e.getCode() == KeyCode.DOWN) {
                Platform.runLater(() -> {
                    listView.requestFocus();
                    if (listView.getSelectionModel().isEmpty()) {
                        listView.getSelectionModel().selectFirst();
                    }
                });
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                popup.hide();
            }
        });

        // --- KEYBOARD (INSIDE LIST) ---
        listView.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> {
                    applySelection(field, listView, popup, ignoreTextChange);
                    popup.hide();
                    field.requestFocus();
                    e.consume();
                }
                case ESCAPE -> {
                    popup.hide();
                    field.requestFocus();
                    e.consume();
                }
                default -> { }
            }
        });

        // --- HIDE WHEN FIELD LOSES FOCUS COMPLETELY ---
        field.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused && !listView.isFocused()) {
                popup.hide();
            }
        });
    }

    // ==========================================================

    private static void applySelection(TextField field,
                                       ListView<String> listView,
                                       Popup popup,
                                       boolean[] ignoreTextChange) {

        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        ignoreTextChange[0] = true;
        field.setText(selected);
        ignoreTextChange[0] = false;

        popup.hide();
        field.requestFocus();
        field.positionCaret(selected.length());
    }

    private static void showPopup(TextField field, Popup popup) {
        Bounds b = field.localToScreen(field.getBoundsInLocal());
        if (b == null) return;

        double x = b.getMinX() - field.snappedLeftInset();
        double y = b.getMaxY() - field.snappedBottomInset();

        popup.show(field.getScene().getWindow(), Math.round(x), Math.round(y)
        );
    }
}
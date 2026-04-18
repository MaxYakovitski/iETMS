package com.mayak.ietms.infrastructure.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultiSelectComboBoxUtils {

    public static <T> void setupMultiSelect(
            CheckComboBox<T> checkComboBox,
            List<T> sourceList,
            String promptText,
            Function<T, String> displayFunction
    ) {
        checkComboBox.getItems().setAll(FXCollections.observableArrayList(sourceList));
        setupConverter(checkComboBox, promptText, displayFunction);
        clearCheckComboBoxFocus(checkComboBox);
    }

    public static <T> void setupMultiSelectWithAll(
            CheckComboBox<T> checkComboBox,
            List<T> sourceList,
            T allItem,
            String promptText,
            Function<T, String> displayFunction
    ) {

        List<T> fullList = new ArrayList<>();
        fullList.add(allItem);
        fullList.addAll(sourceList);

        checkComboBox.getItems().setAll(FXCollections.observableArrayList(fullList));
        setupConverter(checkComboBox, promptText, displayFunction);

        var model = checkComboBox.getCheckModel();
        BooleanProperty bulk = new SimpleBooleanProperty(false);

        model.getCheckedItems().addListener((ListChangeListener<T>) change -> {
            if (bulk.get()) return;
            bulk.set(true);

            while (change.next()) {
                if (change.wasAdded() && change.getAddedSubList().contains(allItem)) {
                    model.checkAll();
                }
                else if (change.wasRemoved() && change.getRemoved().contains(allItem)) {
                    model.clearChecks();
                }
                else {
                    int realChecked = (int) model.getCheckedItems().stream()
                            .filter(i -> i != allItem).count();
                    if (realChecked == sourceList.size()) model.check(allItem);
                    else model.clearCheck(allItem);
                }
            }

            bulk.set(false);
        });

        clearCheckComboBoxFocus(checkComboBox);
    }

    public static <T> void setupSingleSelect(
            ComboBox<T> comboBox,
            List<T> sourceList,
            Function<T, String> displayFunction
    ) {
        comboBox.getItems().setAll(sourceList);

        comboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : displayFunction.apply(item));
            }
        });

        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null
                        ? comboBox.getPromptText()
                        : displayFunction.apply(item));
            }
        });

        clearComboBoxFocus(comboBox);
    }

    private static <T> void setupConverter(
            CheckComboBox<T> comboBox,
            String promptText,
            Function<T, String> displayFunction
    ) {
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object != null ? displayFunction.apply(object) : "";
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });

        updateButtonText(comboBox, promptText, displayFunction);

        comboBox.getCheckModel().getCheckedItems().addListener(
                (ListChangeListener<T>) item ->
                        updateButtonText(comboBox, promptText, displayFunction)
        );
    }

    private static <T> void updateButtonText(
            CheckComboBox<T> checkComboBox,
            String promptText,
            Function<T, String> displayFunction
    ) {
        String selectedText = checkComboBox.getCheckModel().getCheckedItems().stream()
                .map(displayFunction)
                .collect(Collectors.joining(", "));

        if (selectedText.isEmpty()) {
            checkComboBox.setTitle(promptText);
        } else {
            checkComboBox.setTitle(selectedText);
        }
    }

    public static void clearCheckComboBoxFocus(CheckComboBox<?> checkComboBox) {
        var listView =
                (ListView<?>) checkComboBox.lookup(".list-view");
        if (listView != null) {
            listView.getFocusModel().focus(-1);
            listView.getSelectionModel().clearSelection();
        }
    }

    public static void clearComboBoxFocus(ComboBox<?> comboBox) {
        var listView = (ListView<?>) comboBox.lookup(".list-view");
        if (listView != null) {
            listView.getFocusModel().focus(-1);
            listView.getSelectionModel().clearSelection();
        }

        comboBox.getSelectionModel().clearSelection();
        comboBox.setValue(null);
    }
}
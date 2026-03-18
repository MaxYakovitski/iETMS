package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.statistics.ReportType;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.integration.api.ReportClient;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.support.validation.DateRange;
import com.mayak.ietms.support.validation.DateRangeUiValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
public class ReportController {
    @FXML public ComboBox<ReportType> reportChoiceBox;
    @FXML public DatePicker startDatePicker, endDatePicker;
    @FXML public Button downloadButton;
    @FXML public VBox formContainer;
    @FXML public ProgressBar progressBar;

    private final ReportClient reportClient;
    private ValidationUIHelper validationUI;

    @Getter @Setter
    private Stage stage;
    @Getter @Setter
    private UserResponseDto loggedInUser;

    @FXML
    public void initialize() {
        DatePickerUtils.setupDatePickers(startDatePicker, endDatePicker);
        reportChoiceBox.getItems().setAll(ReportType.values());

        Map<String, Control> fieldMap = Map.of(
                "startDate", startDatePicker,
                "endDate", endDatePicker,
                "report", reportChoiceBox
        );

        validationUI = new ValidationUIHelper(fieldMap);
        validationUI.bindResetOnChange();
    }

    @FXML
    public void handleDownload() {

        DateRangeUiValidator dateRangeUiValidator = new DateRangeUiValidator();
        var result = dateRangeUiValidator.isValid(new DateRange(startDatePicker, endDatePicker));

        if (reportChoiceBox.getValue() == null) {
            result.add(new ValidationError("report", "Please select report"));
        }

        if (!result.isValid()) {
            validationUI.showClientErrors(result.getErrors());
            return;
        }

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        ReportType type = reportChoiceBox.getValue();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save report");
        fileChooser.setInitialFileName("report_" + type.toString().toLowerCase() + "_" +start + "_to_" + end + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File selectedFile = fileChooser.showSaveDialog(reportChoiceBox.getScene().getWindow());
        if (selectedFile == null) return;

        formContainer.setVisible(false);
        formContainer.setManaged(false);
        progressBar.setVisible(true);
        progressBar.setManaged(true);

        CompletableFuture.runAsync(() -> {
            try {
                reportClient.downloadRequestsReport(type, start, end, selectedFile,
                        progress -> Platform.runLater(() -> progressBar.setProgress(progress)));
            } catch (Exception e) {
                Platform.runLater(() ->
                        AlertUtils.showError("Failed to generate report: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                    formContainer.setVisible(true);
                    formContainer.setManaged(true);
                });
            }
        });
    }
}
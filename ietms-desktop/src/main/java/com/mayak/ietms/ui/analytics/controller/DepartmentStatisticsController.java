package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.integration.api.DepartmentClient;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.statistics.DepartmentStatsDto;
import com.mayak.ietms.statistics.RefuseReasonCountDto;
import com.mayak.ietms.ui.analytics.service.DepartmentAnalyticsFacade;
import com.mayak.ietms.ui.analytics.view.DepartmentChartsRenderer;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.infrastructure.common.ResetUtils;
import com.mayak.ietms.infrastructure.ui.DepartmentUIHelper;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.support.validation.DateRange;
import com.mayak.ietms.support.validation.DateRangeUiValidator;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
public class DepartmentStatisticsController extends BaseStatisticsController {

    @FXML public Label departmentFullNameLabel;
    @FXML public DatePicker startDatePicker, endDatePicker;
    @FXML public VBox reportContainer;
    @FXML public HBox placeholderContainer;
    @FXML public BarChart<Number, String> barChartSpotRefusedReason, barChartContractRefusedReason;
    @FXML public StackPane allContainer, spotBidedContainer, spotEfficiencyContainer, contractEfficiencyContainer,
            spotEmptyStack, contractEmptyStack;
    @FXML public LineChart <String, Number>lineChartCompression;
    @FXML public ComboBox<DepartmentDto> departmentComboBox;

    @Getter @Setter
    private HomeController homeController;

    private final DepartmentAnalyticsFacade analytics;
    private final DepartmentChartsRenderer charts;
    private final DepartmentClient departmentClient;

    @FXML
    public void initialize() {
        DatePickerUtils.setupDatePickers(startDatePicker, endDatePicker);
        resetFields();
    }

    @Override
    public void onShow() {

        List<DepartmentDto> departments = permissions.isAdmin()
                ? departmentClient.findAll()
                : List.of();

        DepartmentUIHelper.setupDepartmentSelector(
                loggedInUser,
                permissions,
                departmentFullNameLabel,
                departmentComboBox,
                departments
        );
    }

    @FXML
    public void handleFormReport() {
        Map<String, Control> fieldMap = Map.of(
                "startDate", startDatePicker,
                "endDate", endDatePicker,
                "departments", departmentComboBox
        );

        DateRangeUiValidator dateRangeUiValidator = new DateRangeUiValidator();
        var result = dateRangeUiValidator.isValid(new DateRange(startDatePicker, endDatePicker));


        if (getUserDepartmentId() == null
                && departmentComboBox.getSelectionModel().isEmpty()) {
            result.add(new ValidationError("departments", "Department is required"));
        }

        if (!result.isValid()) {
            new ValidationUIHelper(fieldMap).showClientErrors(result.getErrors());
            return;
        }

        DepartmentStatsDto stats = analytics.loadDepartmentStats(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                getEffectiveDepartmentId()
        );

        render(stats);

        placeholderContainer.setVisible(false);
        placeholderContainer.setManaged(false);
        reportContainer.setVisible(true);
        reportContainer.setManaged(true);
    }

    // ---------- RENDER ----------
    private void render(DepartmentStatsDto stats) {

        charts.renderProgressRing(
                stats.spotTotal(), stats.contractTotal(),
                "Spot", "Contract",
                DepartmentChartsRenderer.COLOR_COMMON,
                allContainer);

        charts.renderProgressRing(
                stats.spotBided(), stats.spotNotBided(),
                "Bided", "Not bided",
                DepartmentChartsRenderer.COLOR_COMMON,
                spotBidedContainer);

        charts.renderProgressRing(
                stats.spotAccepted(), stats.spotRefused(),
                "Accepted", "Refused",
                DepartmentChartsRenderer.COLOR_EFFICIENCY,
                spotEfficiencyContainer);

        charts.renderProgressRing(
                stats.contractAccepted(), stats.contractRefused(),
                "Accepted", "Refused",
                DepartmentChartsRenderer.COLOR_EFFICIENCY,
                contractEfficiencyContainer);

        charts.renderBar(toBarData(stats.spotRefusedByReason()), barChartSpotRefusedReason, spotEmptyStack);
        charts.renderBar(toBarData(stats.contractRefusedByReason()), barChartContractRefusedReason, contractEmptyStack);

        charts.renderCompression(stats.monthlyCompression(), lineChartCompression);
    }

    private Map<String, Integer> toBarData(List<RefuseReasonCountDto> list) {
        if (list == null || list.isEmpty()) return Map.of();

        return list.stream().collect(Collectors.toMap(
                RefuseReasonCountDto::reasonCode,
                RefuseReasonCountDto::count,
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private void resetFields() {
        ResetUtils.resetOnChange(startDatePicker, startDatePicker.valueProperty());
        ResetUtils.resetOnChange(endDatePicker, endDatePicker.valueProperty());
    }

    @Override
    protected Long getSelectedDepartmentId() {
        return departmentComboBox.getValue() != null
                ? departmentComboBox.getValue().id()
                : null;
    }
}
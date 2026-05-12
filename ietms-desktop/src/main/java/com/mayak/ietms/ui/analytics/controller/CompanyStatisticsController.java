package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.integration.analytics.AnalyticsClient;
import com.mayak.ietms.integration.api.DepartmentClient;
import com.mayak.ietms.analytics.AnalyticsFilterDto;
import com.mayak.ietms.analytics.AnalyticsReportDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.statistics.CompanyStatsDto;
import com.mayak.ietms.ui.analytics.model.CompanyReport;
import com.mayak.ietms.ui.analytics.model.CompanyReportItem;
import com.mayak.ietms.ui.analytics.view.CompanyReportRenderer;
import com.mayak.ietms.ui.core.RequiresPermission;
import com.mayak.ietms.ui.core.ViewPermission;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.infrastructure.fx.MultiSelectComboBoxUtils;
import com.mayak.ietms.infrastructure.common.ResetUtils;
import com.mayak.ietms.infrastructure.ui.DepartmentUIHelper;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.support.validation.DateRange;
import com.mayak.ietms.support.validation.DateRangeUiValidator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.CheckComboBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * Analytics screen showing per-company request statistics for a department.
 *
 * <p>Companies available for selection are loaded dynamically based on
 * the chosen department and date range — the combo box is disabled
 * until both are provided.
 */
@Controller
@FxmlView("statistics_companies.fxml")
@Scope("prototype")
@RequiresPermission(ViewPermission.ANALYTICS)
@RequiredArgsConstructor
@Slf4j
public class CompanyStatisticsController extends BaseStatisticsController {

    @FXML
    public Label departmentFullNameLabel, emptyMessageLabel;

    @FXML
    public DatePicker startDatePicker, endDatePicker;

    @FXML
    public CheckComboBox<CompanyDto> companiesCheckComboBox;

    @FXML
    public VBox reportContainer;

    @FXML
    public HBox placeholderContainer;

    @FXML
    public ComboBox<DepartmentDto> departmentComboBox;

    @Getter @Setter
    private HomeController homeController;

    private final AnalyticsClient analyticsClient;
    private final DepartmentClient departmentClient;
    private final CompanyReportRenderer companyReportRenderer;

    private static final String COMPANY_SELECTED = "customer";

    @FXML
    public void initialize() {
        DatePickerUtils.setupDatePickers(startDatePicker, endDatePicker);
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

        companiesCheckComboBox.setTitle(COMPANY_SELECTED);
        companiesCheckComboBox.setDisable(true);

        if (getUserDepartmentId() == null) {
            departmentComboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldDep, newDep) -> tryLoadCompanies());
        }

        startDatePicker.valueProperty().addListener((obs, o, n) -> tryLoadCompanies());
        endDatePicker.valueProperty().addListener((obs, o, n) -> tryLoadCompanies());

        resetFields();
    }

    @FXML
    public void handleFormReport() {
        Map<String, Control> fieldMap = Map.of(
                "startDate", startDatePicker,
                "endDate", endDatePicker,
                "companies", companiesCheckComboBox,
                "departments", departmentComboBox
        );

        var result = validateReportForm();
        if (!result.isValid()) {
            new ValidationUIHelper(fieldMap).showClientErrors(result.getErrors());
            return;
        }

        List<CompanyDto> selectedCompanies = companiesCheckComboBox.getCheckModel()
                .getCheckedItems()
                .stream()
                .filter(c -> c.id() != -1L)
                .toList();

        Long departmentId = getEffectiveDepartmentId();

        AnalyticsFilterDto filter = new AnalyticsFilterDto(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                departmentId,
                selectedCompanies.stream().map(CompanyDto::id).toList(),
                null
        );

        AnalyticsReportDto report = analyticsClient.getAnalytics(filter);
        showCompaniesReport(report != null ? report.companies() : List.of());
    }

    private void loadCompaniesForStatistics(Long departmentId) {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) return;
        CompanyDto ALL = new CompanyDto(-1L, "all");

        List<CompanyDto> companies =
                analyticsClient.findCompaniesForDepartmentAnalytics(
                        departmentId,
                        startDatePicker.getValue(),
                        endDatePicker.getValue()
                );

        MultiSelectComboBoxUtils.setupMultiSelectWithAll(
                companiesCheckComboBox,
                companies,
                ALL,
                COMPANY_SELECTED,
                CompanyDto::name
        );

    }

    private void tryLoadCompanies() {
        Long departmentId =
                getUserDepartmentId() != null
                        ? getUserDepartmentId()
                        : departmentComboBox.getValue() != null
                        ? departmentComboBox.getValue().id()
                        : null;

        if (departmentId == null) return;
        if (startDatePicker.getValue() == null) return;
        if (endDatePicker.getValue() == null) return;

        loadCompaniesForStatistics(departmentId);
        companiesCheckComboBox.setDisable(false);
    }

    private ValidationResult validateReportForm() {
        DateRangeUiValidator dateRangeUiValidator = new DateRangeUiValidator();
        var result = dateRangeUiValidator.isValid(new DateRange(startDatePicker, endDatePicker));
        if (getUserDepartmentId() == null
                && departmentComboBox.getSelectionModel().isEmpty()) {
            result.add(new ValidationError("departments", "Department is required"));
        }
        if (companiesCheckComboBox.getCheckModel().getCheckedItems().isEmpty()) {
            result.add(new ValidationError("companies", "Please select at least one customerName"));
        }
        return result;
    }

    private void showCompaniesReport(List<CompanyStatsDto> stats) {
        placeholderContainer.setVisible(false);
        placeholderContainer.setManaged(false);

        reportContainer.getChildren().clear();

        boolean empty = (stats == null || stats.isEmpty());
        emptyMessageLabel.setVisible(empty);
        emptyMessageLabel.setManaged(empty);

        reportContainer.setVisible(!empty);
        reportContainer.setManaged(!empty);

        if (empty) return;

        stats.stream()
                .map(this::toUiModel)
                .map(companyReportRenderer::render)
                .forEach(reportContainer.getChildren()::add);
    }

    private CompanyReport toUiModel(CompanyStatsDto dto) {
        return new CompanyReport(
                new CompanyDto(dto.companyId(), dto.companyName()),
                dto.items().stream()
                        .map(i -> new CompanyReportItem(
                                i.lane(),
                                i.transportType(),
                                i.spotCount(),
                                i.spotEfficiencyPercent(),
                                i.spotProfit(),
                                i.contractCount(),
                                i.contractEfficiencyPercent(),
                                i.contractProfit()
                        ))
                        .toList()
        );
    }

    @Override
    protected Long getSelectedDepartmentId() {
        return departmentComboBox.getValue() != null
                ? departmentComboBox.getValue().id()
                : null;
    }

    private void resetFields() {
        ResetUtils.resetOnChange(startDatePicker, startDatePicker.valueProperty());
        ResetUtils.resetOnChange(endDatePicker, endDatePicker.valueProperty());
        ResetUtils.resetOnCheckChange(companiesCheckComboBox);
    }
}
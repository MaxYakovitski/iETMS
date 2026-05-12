package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.integration.api.DepartmentClient;
import com.mayak.ietms.integration.api.UserClient;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.ui.core.RequiresPermission;
import com.mayak.ietms.ui.core.ViewPermission;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserNameDto;
import com.mayak.ietms.ui.analytics.model.UserReport;
import com.mayak.ietms.ui.analytics.service.UserAnalyticsFacade;
import com.mayak.ietms.ui.analytics.view.UserReportRenderer;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.infrastructure.fx.MultiSelectComboBoxUtils;
import com.mayak.ietms.infrastructure.common.ResetUtils;
import com.mayak.ietms.infrastructure.ui.DepartmentUIHelper;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.support.validation.DateRange;
import com.mayak.ietms.support.validation.DateRangeUiValidator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxmlView;
import org.controlsfx.control.CheckComboBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Analytics screen showing per-employee request statistics.
 *
 * <p>Users available for selection are loaded dynamically based on
 * the chosen department. For non-admin users the department is fixed
 * to their own and the selector is hidden.
 */
@Controller
@FxmlView("statistics_employees.fxml")
@Scope("prototype")
@RequiresPermission(ViewPermission.ANALYTICS)
@RequiredArgsConstructor
public class UserStatisticsController extends BaseStatisticsController {

    @FXML
    public Label departmentFullNameLabel;

    @FXML
    public ComboBox <DepartmentDto> departmentComboBox;

    @FXML
    public DatePicker startDatePicker, endDatePicker;

    @FXML
    public CheckComboBox<UserLookupDto> usersComboBox;

    @FXML
    public VBox reportContainer;

    @FXML
    public HBox placeholderContainer;

    private final DepartmentClient departmentClient;
    private final UserClient userClient;
    private final UserAnalyticsFacade analytics;
    private final UserReportRenderer userReportRenderer;

    private static final String USER_SELECTED = "employee";
    private static final UserLookupDto ALL = new UserLookupDto(-1L, new UserNameDto("all", ""));

    @Getter @Setter
    private HomeController homeController;

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

        usersComboBox.setTitle(USER_SELECTED);

        if (getUserDepartmentId() != null) {
            loadUsersForDepartment(getUserDepartmentId());
        } else {
            usersComboBox.setDisable(true);
            departmentComboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((dep, oldDep, newDept) -> {
                        if (newDept != null) {
                            loadUsersForDepartment(newDept.id());
                            usersComboBox.setDisable(false);
                        }
                    });
        }
        resetFields();
    }

    @FXML
    public void handleFormReport() {
        Map<String, Control> fieldMap = Map.of(
                "startDate", startDatePicker,
                "endDate", endDatePicker,
                "users", usersComboBox,
                "departments", departmentComboBox
        );

        var result = validateReportForm();
        if (!result.isValid()) {
            new ValidationUIHelper(fieldMap).showClientErrors(result.getErrors());
            return;
        }

        List<Long> userIds = usersComboBox.getCheckModel()
                .getCheckedItems()
                .stream()
                .map(UserLookupDto::id)
                .filter(id -> !Objects.equals(id, ALL.id()))
                .toList();

        Long departmentId = getEffectiveDepartmentId();

        List<UserReport> reports = analytics.loadUserReports(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                departmentId,
                userIds
        );

        showReport(reports);
    }

    @Override
    protected Long getSelectedDepartmentId() {
        return departmentComboBox.getValue() != null
                ? departmentComboBox.getValue().id()
                : null;
    }

    private ValidationResult validateReportForm() {
        DateRangeUiValidator dateRangeUiValidator = new DateRangeUiValidator();
        var result = dateRangeUiValidator.isValid(new DateRange(startDatePicker, endDatePicker));
        if (getUserDepartmentId() == null
                && departmentComboBox.getSelectionModel().isEmpty()) {
            result.add(new ValidationError("departments", "Department is required"));
        }
        if (usersComboBox.getCheckModel().getCheckedItems().isEmpty()) {
            result.add(new ValidationError("users", "Please select at least one user"));
        }
        return result;
    }

    private void loadUsersForDepartment(Long departmentId) {
        List<UserLookupDto> users =
                userClient.findColleaguesLookupByDepartment(departmentId);

        MultiSelectComboBoxUtils.setupMultiSelectWithAll(
                usersComboBox,
                users,
                ALL,
                USER_SELECTED,
                dto -> dto.name().fullName()
        );
    }

    private void showReport(List<UserReport> reports) {
        placeholderContainer.setVisible(false);
        placeholderContainer.setManaged(false);

        reportContainer.getChildren().clear();
        reports.forEach(r ->
                reportContainer.getChildren().add(userReportRenderer.render(r))
        );
    }

    private void resetFields() {
        ResetUtils.resetOnChange(startDatePicker, startDatePicker.valueProperty());
        ResetUtils.resetOnChange(endDatePicker, endDatePicker.valueProperty());
        ResetUtils.resetOnCheckChange(usersComboBox);
    }
}
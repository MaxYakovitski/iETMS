package com.mayak.ietms.ui.administration;

import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.department.dto.DepartmentCreateDto;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.integration.api.DepartmentClient;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.common.ResetUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.ui.core.RequiresPermission;
import com.mayak.ietms.ui.core.ViewPermission;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * Administration screen for managing organisational departments.
 * Departments are referenced by users and cannot be deleted while in use.
 */
@Controller
@FxmlView("settings_department.fxml")
@Scope("prototype")
@RequiresPermission(ViewPermission.ADMINISTRATION)
@RequiredArgsConstructor
@Slf4j
public class DepartmentSettingsController extends AbstractSettingsController<DepartmentDto, DepartmentDto, DepartmentCreateDto> {

    private final DepartmentClient departmentClient;

    @FXML
    private TextField deptNameField, deptCodeField;

    @FXML
    private TableView<DepartmentDto> departmentsTable;

    @FXML
    private TableColumn<DepartmentDto, String> deptCodeColumn, deptNameColumn;

    @FXML
    private Button addButton, removeButton, editButton;

    @Override
    protected Long extractId(DepartmentDto view) {
        return view.id();
    }

    @Override
    protected Button getAddButton() {
        return addButton;
    }

    @Override
    protected Button getEditButton() {
        return editButton;
    }

    @Override
    protected Button getRemoveButton() {
        return removeButton;
    }

    @FXML
    public void initialize() {
        TextUtils.allowOnlyLatin(deptNameField, deptCodeField);
        departmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        deptCodeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().code()));
        deptNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().name()));

        initValidation();
    }

    @Override
    protected DepartmentCreateDto buildCreateDto() {
        return new DepartmentCreateDto(
                deptNameField.getText().trim(),
                deptCodeField.getText().trim()
        );
    }

    @Override
    protected DepartmentDto buildUpdateDto(Long id) {
        return new DepartmentDto(
                id,
                deptNameField.getText().trim(),
                deptCodeField.getText().trim()
        );
    }

    @Override
    protected void add(DepartmentCreateDto dto) {
        departmentClient.create(dto);
    }

    @Override
    protected void update(DepartmentDto dto) {
        departmentClient.update(dto);
    }


    @Override
    protected void remove(DepartmentDto department) {
        boolean ok =
                AlertUtils.showConfirmation(null,
                        "Are you sure that you want to delete this department? " +
                        "This action cannot be undone.");

        if (!ok) return;

        try {
            departmentClient.delete(department.id());
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex,
                    "This department cannot be deleted because it is used by other records."));
        }
    }

    @Override
    protected Iterable<DepartmentDto> loadAll() {
        return departmentClient.findAll();
    }

    @Override
    protected void fillForm(DepartmentDto department) {
        deptNameField.setText(department.name());
        deptCodeField.setText(department.code());
    }

    @Override
    protected void resetFields() {
        ResetUtils.reset(deptNameField, deptCodeField);
    }

    @Override
    protected TableView<DepartmentDto> getTable() {
        return departmentsTable;
    }

    @Override
    protected Map<String, Control> getFieldMap() {
        return Map.of(
                "name", deptNameField,
                "code", deptCodeField);
    }

    @Override
    protected boolean canEdit() {
        return permissions != null && permissions.canViewAdministration();
    }
}
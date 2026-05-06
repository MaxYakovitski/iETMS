package com.mayak.ietms.ui.administration;

import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.domain.user.UserFormPolicy;
import com.mayak.ietms.domain.user.UserFormState;
import com.mayak.ietms.department.dto.DepartmentDto;
import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;
import com.mayak.ietms.user.dto.enums.UserStatusDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import com.mayak.ietms.integration.api.DepartmentClient;
import com.mayak.ietms.integration.api.UserClient;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.fx.ComboBoxUtils;
import com.mayak.ietms.infrastructure.common.ResetUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.form.UserForm;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.Map;

/**
 * Administration screen controller for managing users.
 * Supports creating, editing, deleting users, and toggling their active status.
 */
@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class UserSettingsController extends AbstractSettingsController<UserResponseDto, UserUpdateDto, UserCreateDto> {

    private final UserClient userClient;
    private final DepartmentClient departmentClient;

    private final UserForm form = new UserForm();

    private final UserFormPolicy formPolicy = new UserFormPolicy();

    @FXML public TextField userNameField, userSurnameField, userEmailField, userPasswordField;
    @FXML public ComboBox <UserTypeDto> userTypeCombo;
    @FXML public ComboBox <RoleDto> userRoleCombo;
    @FXML public ComboBox <PriorityDto> userPriorityCombo;
    @FXML public ComboBox <DepartmentDto> userDepartmentCombo;
    @FXML Button addButton, removeButton, editButton, toggleLicenseButton;

    @FXML private TableView<UserResponseDto> usersTable;
    @FXML public TableColumn <UserResponseDto, String> userNameColumn, userSurnameColumn, userEmailColumn,
            userTypeColumn, userRoleColumn, userPriorityColumn, userDeptColumn, userStatusColumn;

    @FXML
    public void initialize() {
        TextUtils.allowOnlyLatin(userNameField, userSurnameField, userEmailField, userPasswordField);

        setupTable();
        setupCombos();
        initValidation();

        userTypeCombo.valueProperty()
                .addListener((type, oldType, newType) -> applyFormPolicy(newType));
    }

    private void setupTable() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        userNameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        userSurnameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().surname()));
        userEmailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().email()));
        userTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().userType().name()));

        userRoleColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().profile() != null && c.getValue().profile().role() != null
                        ? c.getValue().profile().role().name()
                        : "")
        );

        userPriorityColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().profile() != null && c.getValue().profile().priority() != null
                        ? c.getValue().profile().priority().name()
                        : "")
        );

        userDeptColumn.setCellValueFactory(c ->
                new SimpleStringProperty( c.getValue().profile() != null
                        ? c.getValue().profile().departmentName()
                        : "")
        );

        userStatusColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().status().name()));

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            toggleLicenseButton.setVisible(true);
            boolean isActive = newVal.status() == UserStatusDto.ACTIVE;
            toggleLicenseButton.setText(isActive ? "deactivate" : "activate");
            toggleLicenseButton.getStyleClass().removeAll("deactivate-outline", "activate-outline");
            toggleLicenseButton.getStyleClass().add(isActive ? "deactivate-outline" : "activate-outline");
        });
    }

    private void setupCombos() {
        userTypeCombo.getItems().setAll(Arrays.stream(UserTypeDto.values())
                .filter(type -> type != UserTypeDto.ADMIN).toList());
        userRoleCombo.getItems().setAll(RoleDto.values());
        userPriorityCombo.getItems().setAll(PriorityDto.values());
        userDepartmentCombo.getItems().setAll(departmentClient.findAll());

        ComboBoxUtils.setupPrompt(userTypeCombo, Enum::name);
        ComboBoxUtils.setupPrompt(userRoleCombo, Enum::name);
        ComboBoxUtils.setupPrompt(userPriorityCombo, Enum::name);
        ComboBoxUtils.setupPrompt(userDepartmentCombo, DepartmentDto::name);
    }

    private void applyFormPolicy(UserTypeDto userType) {
        UserFormState state = formPolicy.apply(userType);

        userRoleCombo.setDisable(!state.roleEnabled());
        userPriorityCombo.setDisable(!state.priorityEnabled());

        if (!state.roleEnabled()) {
            userRoleCombo.setValue(null);
        }
        if (!state.priorityEnabled()) {
            userPriorityCombo.setValue(null);
        }

        userDepartmentCombo.setDisable(false);
        validationUI.clearError("departmentId");
    }

    private void syncFormFromUi() {
        form.setName(userNameField.getText());
        form.setSurname(userSurnameField.getText());
        form.setEmail(userEmailField.getText());
        form.setPassword(userPasswordField.getText());
        form.setUserType(userTypeCombo.getValue());
        form.setRole(userRoleCombo.getValue());
        form.setPriority(userPriorityCombo.getValue());

        DepartmentDto department = userDepartmentCombo.getValue();
        form.setDepartmentId(department != null ? department.id() : null);
    }

    @Override
    protected Long extractId(UserResponseDto view) {
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


    @Override
    protected UserCreateDto buildCreateDto() {
        syncFormFromUi();
        return form.toCreateDto();
    }

    @Override
    protected UserUpdateDto buildUpdateDto(Long id) {
        syncFormFromUi();
        return form.toUpdateDto(id);
    }

    @Override
    protected void fillForm(UserResponseDto user) {
        form.fillFrom(user);

        userNameField.setText(form.getName());
        userSurnameField.setText(form.getSurname());
        userEmailField.setText(form.getEmail());
        userPasswordField.clear();

        userTypeCombo.setValue(form.getUserType());
        userRoleCombo.setValue(form.getRole());
        userPriorityCombo.setValue(form.getPriority());

        if (form.getDepartmentId() != null) {
            userDepartmentCombo.getItems().stream()
                    .filter(d -> d.id().equals(form.getDepartmentId()))
                    .findFirst()
                    .ifPresent(userDepartmentCombo::setValue);
        } else {
            userDepartmentCombo.setValue(null);
        }
    }

    @Override
    protected void resetFields() {
        ResetUtils.reset(
                userNameField,
                userSurnameField,
                userEmailField,
                userPasswordField,
                userTypeCombo,
                userRoleCombo,
                userPriorityCombo,
                userDepartmentCombo
        );
    }

    @Override
    protected void add(UserCreateDto dto) {
        hideAllComboBoxPopups();
        userClient.create(dto);
    }

    @Override
    protected void update(UserUpdateDto dto) {
        hideAllComboBoxPopups();

        userClient.update(dto.getId(), dto);

        String newPassword = userPasswordField.getText();
        if (newPassword != null && !newPassword.isBlank()) {
            userClient.changePassword(dto.getId(), newPassword);
        }
    }

    @Override
    protected void remove(UserResponseDto user) {
        boolean ok =
                AlertUtils.showConfirmation(null, "Are you sure that you want to delete this user? " +
                        "This action cannot be undone.");

        if (!ok) return;

        try {
            userClient.delete(user.id());
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex, "This user cannot be deleted because they are used in existing requests."));
        }
    }

    /**
     * Toggles the status of the selected user between {@code ACTIVE} and {@code INACTIVE}.
     * Reselects the updated user in the table after the change.
     */
    @FXML
    public void handleToggleStatus() {
        UserResponseDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        UserStatusDto newStatus = selected.status() == UserStatusDto.ACTIVE
                ? UserStatusDto.INACTIVE
                : UserStatusDto.ACTIVE;

        try {
            userClient.changeStatus(selected.id(), newStatus);
            loadTable();
            usersTable.getItems().stream()
                    .filter(u -> u.id().equals(selected.id()))
                    .findFirst()
                    .ifPresent(u -> usersTable.getSelectionModel().select(u));
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex, "Failed to change user status."));
        }
    }

    @Override
    protected Iterable<UserResponseDto> loadAll() {
        return userClient.findAll()
                .stream()
                .filter(u -> u.userType() != UserTypeDto.ADMIN)
                .toList();
    }

    @Override
    protected TableView<UserResponseDto> getTable() {
        return usersTable;
    }

    @Override
    protected Map<String, Control> getFieldMap() {
        return Map.of(
                "name", userNameField,
                "surname", userSurnameField,
                "email", userEmailField,
                "password", userPasswordField,
                "type", userTypeCombo,
                "role", userRoleCombo,
                "priority", userPriorityCombo,
                "departmentId", userDepartmentCombo
        );
    }

    @Override
    protected boolean canEdit() {
        return permissions != null && permissions.canViewAdministration();
    }

    private void hideAllComboBoxPopups() {
        userTypeCombo.hide();
        userRoleCombo.hide();
        userPriorityCombo.hide();
        userDepartmentCombo.hide();
    }
}
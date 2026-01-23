package com.mayak.iet.ui.administration;

import com.mayak.iet.integration.exception.ApiValidationException;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.ui.core.SecuredView;
import com.mayak.iet.ui.core.UserPermissions;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.ui.ValidationUIHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;

import java.util.Map;

public abstract class AbstractSettingsController <V, U, C> implements SecuredView, ViewLifecycle {

    protected UserResponseDto loggedInUser;
    protected UserPermissions permissions;

    protected ValidationUIHelper validationUI;

    protected boolean editingMode = false;
    protected Long editingItemId;

    protected abstract Button getAddButton();
    protected abstract Button getEditButton();
    protected abstract Button getRemoveButton();

    protected abstract TableView<V> getTable();
    protected abstract Iterable<V> loadAll();

    protected abstract Map<String, Control> getFieldMap();

    protected abstract C buildCreateDto();
    protected abstract U buildUpdateDto(Long id);

    protected abstract void add(C dto);
    protected abstract void update(U dto);
    protected abstract void remove(V item);

    protected abstract void fillForm(V view);
    protected abstract void resetFields();

    protected void initValidation() {
        this.validationUI = new ValidationUIHelper(getFieldMap());
        this.validationUI.bindResetOnChange();
    }

    @Override
    public void onShow() {
        loadTable();
    }

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
        applyPermissions();
    }

    protected void applyPermissions() {
        boolean canEdit = permissions != null && permissions.canViewAdministration();

        getAddButton().setDisable(!canEdit);
        getEditButton().setDisable(!canEdit);
        getRemoveButton().setDisable(!canEdit);
    }

    @FXML
    public void handleAddOrSave(ActionEvent e) {
        try {
        if (editingMode && editingItemId != null) {
            update(buildUpdateDto(editingItemId));
            editingMode = false;
            editingItemId = null;
        } else {
            add(buildCreateDto());
        }
            resetFields();
            loadTable();
            } catch (ApiValidationException ex) {
            validationUI.showBackendErrors(ex);
        }
    }


    @FXML
    public void handleEdit() {
        var sel = getTable().getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtils.showError("No selection, please select an item to remove.");
            return;
        }
        fillForm(sel);
        editingItemId = extractId(sel);
        editingMode = true;
    }

    protected abstract Long extractId(V view);

    @FXML
    public void handleRemove() {
        var sel = getTable().getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtils.showError("Nothing selected.");
            return;
        }
        remove(sel);
        loadTable();
    }

    protected void loadTable() {
        var tableItems = getTable().getItems();
        tableItems.clear();
        loadAll().forEach(tableItems::add);
    }
}
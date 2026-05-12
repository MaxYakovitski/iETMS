package com.mayak.ietms.ui.administration;

import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.ApiValidationException;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.UserPermissions;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;

import java.util.Map;

/**
 * Base controller for administration settings screens that manage a list of entities
 * with add, edit, and remove operations.
 *
 * <p>Subclasses must implement the abstract template methods to provide
 * entity-specific behaviour: loading data, building DTOs, performing CRUD calls,
 * and managing form fields.</p>
 *
 * @param <V> the view/display type shown in the table
 * @param <U> the update DTO type
 * @param <C> the create DTO type
 */
public abstract class AbstractSettingsController <V, U, C> implements SecuredView, ViewLifecycle {

    protected UserResponseDto loggedInUser;
    protected UserPermissions permissions;

    protected ValidationUIHelper validationUI;

    protected boolean editingMode = false;
    protected Long editingItemId;

    /** @return the "Add / Save" button for permission binding */
    protected abstract Button getAddButton();

    /** @return the "Edit" button for permission binding */
    protected abstract Button getEditButton();

    /** @return the "Remove" button for permission binding */
    protected abstract Button getRemoveButton();

    /** @return the table displaying the entity list */
    protected abstract TableView<V> getTable();

    /** Loads all entities from the backend for display in the table. */
    protected abstract Iterable<V> loadAll();

    /** @return a map of field name to control, used for validation highlighting */
    protected abstract Map<String, Control> getFieldMap();

    /** Builds a create DTO from the current form state. */
    protected abstract C buildCreateDto();

    /** Builds an update DTO from the current form state for the given entity id. */
    protected abstract U buildUpdateDto(Long id);

    /** Sends a create request to the backend. */
    protected abstract void add(C dto);

    /** Sends an update request to the backend. */
    protected abstract void update(U dto);

    /** Sends a delete request to the backend for the given item. */
    protected abstract void remove(V item);

    /** Fills the form fields from the selected item for editing. */
    protected abstract void fillForm(V view);

    /** Resets all form fields to their default/empty state. */
    protected abstract void resetFields();

    /** @return {@code true} if the current user has permission to add, edit, or remove items */
    protected abstract boolean canEdit();

    /**
     * Initialises {@link ValidationUIHelper} from {@link #getFieldMap()}.
     * Must be called from subclass {@code initialize()}.
     */
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
        boolean canEdit = canEdit();

        getAddButton().setDisable(!canEdit);
        getEditButton().setDisable(!canEdit);
        getRemoveButton().setDisable(!canEdit);
    }

    @FXML
    public void handleAddOrSave() {
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
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex, "Operation failed."));
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

    /** Extracts the entity id from the given view item. */
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

    /**
     * Reloads the table from the backend.
     * Called after every successful add, edit, or remove operation.
     */
    protected void loadTable() {
        var tableItems = getTable().getItems();
        tableItems.clear();
        loadAll().forEach(tableItems::add);
    }
}
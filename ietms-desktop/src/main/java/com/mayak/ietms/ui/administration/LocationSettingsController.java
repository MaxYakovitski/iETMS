package com.mayak.ietms.ui.administration;

import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.location.dto.LocationCreateDto;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.integration.api.LocationClient;
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
 * Administration screen for managing location dictionary entries (country, ZIP, place name).
 * Locations are referenced by requests and cannot be deleted while in use.
 */
@Controller
@FxmlView("settings_location.fxml")
@Scope("prototype")
@RequiresPermission(ViewPermission.ADMINISTRATION)
@RequiredArgsConstructor
@Slf4j
public class LocationSettingsController extends AbstractSettingsController<LocationDto, LocationDto, LocationCreateDto> {

    private final LocationClient locationClient;

    @FXML
    private TextField countryCodeField, zipCodeField, placeNameField;

    @FXML
    private TableView<LocationDto> locationsTable;

    @FXML
    private TableColumn<LocationDto, String> locIsoCodeColumn, locZipCodeColumn, locNameColumn;

    @FXML
    private Button addButton, editButton, removeButton;

    @Override
    protected Long extractId(LocationDto view) {
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
        TextUtils.allowOnlyLatin(countryCodeField, zipCodeField, placeNameField);
        locationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        locIsoCodeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().countryCode()));
        locZipCodeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().zipCode()));
        locNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().placeName()));

        initValidation();
    }


    @Override
    protected LocationCreateDto buildCreateDto() {
        return new LocationCreateDto(
                countryCodeField.getText().trim(),
                zipCodeField.getText().trim(),
                placeNameField.getText().trim()
        );
    }

    @Override
    protected LocationDto buildUpdateDto(Long id) {
        return new LocationDto(
                id,
                countryCodeField.getText().trim(),
                zipCodeField.getText().trim(),
                placeNameField.getText().trim()
        );
    }

    @Override
    protected void fillForm(LocationDto location) {
        countryCodeField.setText(location.countryCode());
        zipCodeField.setText(location.zipCode());
        placeNameField.setText(location.placeName());
    }

    @Override
    protected void resetFields() {
        ResetUtils.reset(countryCodeField, zipCodeField, placeNameField);
    }

    @Override
    protected void add(LocationCreateDto dto) {
        locationClient.create(dto);
    }

    @Override
    protected void update(LocationDto dto) {
        locationClient.update(dto);
    }

    @Override
    protected void remove(LocationDto location) {
        boolean ok =
                AlertUtils.showConfirmation(null,
                        "Are you sure that you want to delete this location? " +
                        "This action cannot be undone.");

        if (!ok) return;

        try {
            locationClient.delete(location.id());
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex,
                    "This location cannot be deleted because it is used in existing requests."));
        }
    }

    @Override
    protected Iterable<LocationDto> loadAll() {
        return locationClient.findAll();
    }

    @Override
    protected TableView<LocationDto> getTable() {
        return locationsTable;
    }

    @Override
    protected Map<String, Control> getFieldMap() {
        return Map.of(
                "countryCode", countryCodeField,
                "zipCode", zipCodeField,
                "placeName", placeNameField
        );
    }

    @Override
    protected boolean canEdit() {
        return permissions != null && permissions.canViewAdministration();
    }
}
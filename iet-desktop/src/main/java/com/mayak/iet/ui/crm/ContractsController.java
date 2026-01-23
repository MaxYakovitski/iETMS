package com.mayak.iet.ui.crm;

import com.mayak.iet.integration.api.CompanyClient;
import com.mayak.iet.integration.api.LaneClient;
import com.mayak.iet.integration.exception.ApiException;
import com.mayak.iet.integration.exception.ApiValidationException;
import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.lane.dto.LaneCreateDto;
import com.mayak.iet.lane.dto.LaneViewDto;
import com.mayak.iet.lane.dto.LaneTypeDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.ui.core.SecuredView;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.domain.contract.ContractsFormPolicy;
import com.mayak.iet.ui.crm.form.ContractFormState;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.common.ResetUtils;
import com.mayak.iet.infrastructure.common.TextUtils;
import com.mayak.iet.infrastructure.fx.MultiSelectComboBoxUtils;
import com.mayak.iet.infrastructure.form.LaneForm;
import com.mayak.iet.ui.home.HomeController;
import com.mayak.iet.common.util.formatting.LocationTextFormatter;
import com.mayak.iet.infrastructure.time.DatePickerUtils;
import com.mayak.iet.infrastructure.ui.ValidationUIHelper;
import com.mayak.iet.common.validation.ValidationError;
import com.mayak.iet.lane.validator.LaneContractValidator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class ContractsController implements SecuredView,ViewLifecycle {

    @FXML public TableView<CompanyDto> companiesTable;
    @FXML public TableColumn<CompanyDto, String>companiesColumn;

    @FXML public TableView<LaneViewDto> lanesTable;
    @FXML public TableColumn<LaneViewDto, String> laneIdColumn, fromColumn, toColumn, shipmentTypeColumn, transportTypeColumn,
            tempColumn, laneTypeColumn;
    @FXML public TableColumn<LaneViewDto, Double> weightColumn;
    @FXML public TableColumn<LaneViewDto, BigDecimal> priceColumn, fuelSurchargeColumn, totalPriceColumn;
    @FXML public TableColumn <LaneViewDto, LocalDate>validFromColumn, validToColumn;

    @FXML public TextField laneIdTextField, fromTextField, toTextField, tempTextField, weightTextField, priceTextField, fuelSurchargeTextField;
    @FXML public ComboBox<ShipmentTypeDto> shipmentComboBox;
    @FXML public ComboBox<TransportTypeDto> transportComboBox;
    @FXML public ComboBox<LaneTypeDto> laneTypeComboBox;
    @FXML public DatePicker validFrom, validTo;
    @FXML public Button addButton, removeButton, editButton;

    private final ContractsFormPolicy formPolicy = new ContractsFormPolicy();

    @Getter @Setter
    private HomeController homeController;
    @Getter
    private UserResponseDto loggedInUser;

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
    }

    private final ContractFormState formState = new ContractFormState();

    private final CompanyClient companyClient;
    private  final LaneClient laneClient;
    private final LaneContractValidator laneContractValidator = new LaneContractValidator();
    private LaneForm laneForm;


    public void initialize() {
        TextUtils.allowOnlyLatin(true, laneIdTextField, fromTextField, toTextField, priceTextField,
                fuelSurchargeTextField);
        DatePickerUtils.setupDatePickers(validFrom, validTo);

        laneForm = new LaneForm(
                laneIdTextField,
                fromTextField,
                toTextField,
                tempTextField,
                weightTextField,
                priceTextField,
                fuelSurchargeTextField,
                shipmentComboBox,
                transportComboBox,
                laneTypeComboBox,
                validFrom,
                validTo
        );

        initCompanyTable();
        initLaneTable();
        initComboBoxes();
        setupListeners();
        setupResetUtils();
    }

    private void setupListeners() {
        transportComboBox.getSelectionModel().selectedItemProperty()
                .addListener((selection, oldSelection, newSelection) -> {
            boolean enabled = formPolicy.isTemperatureEnabled(newSelection);
            tempTextField.setDisable(!enabled);
            if (!enabled) tempTextField.clear();
        });
    }

    private void initCompanyTable() {
        companiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        companiesColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        companiesTable.getItems().setAll(companyClient.findAll());
        companiesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((company, oldCompany, newCompany) -> {

                    if (newCompany == null) {
                        formState.switchToCreate();
                        lanesTable.getItems().clear();
                        return;
                    }

                    formPolicy.onCompanySelected(formState, newCompany);
                    fillLaneTable(newCompany.id());
                });
    }

    private void fillLaneTable(Long companyId) {
        lanesTable.getItems().setAll(laneClient.findByCompany(companyId));
    }

    private void initLaneTable() {

        lanesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        laneIdColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().laneName()));

        fromColumn.setCellValueFactory(c ->
                new SimpleStringProperty(LocationTextFormatter.format(c.getValue().fromLocation())));

        toColumn.setCellValueFactory(c ->
                new SimpleStringProperty(LocationTextFormatter.format(c.getValue().toLocation())));

        shipmentTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().shipmentType() != null ?
                                c.getValue().shipmentType().name() : ""
                ));

        transportTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().transportType() != null ?
                                c.getValue().transportType().name() : ""
                ));

        tempColumn.setCellValueFactory(c -> {
            LaneViewDto lane = c.getValue();

            if (lane.transportType() != TransportTypeDto.REF) {
                return new SimpleStringProperty("");
            }

            return new SimpleStringProperty(
                    TextUtils.formatTemperature(lane.temperature())
            );
                });

        weightColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().weight()));

        laneTypeColumn.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().type() != null ?
                                c.getValue().type().name() : ""
                ));

        priceColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().price()));
        setupBigDecimalCell(priceColumn);

        fuelSurchargeColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().fuelSurcharge()));
        setupPercentCell(fuelSurchargeColumn);

        totalPriceColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().totalPrice()));
        setupBigDecimalCell(totalPriceColumn);

        validFromColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().validFrom()));
        validToColumn.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().validTo()));

        setupDateCell(validFromColumn);
        setupDateCell(validToColumn);
    }

    private void initComboBoxes() {
        shipmentComboBox.getItems().setAll(ShipmentTypeDto.values());
        transportComboBox.getItems().setAll(TransportTypeDto.values());
        laneTypeComboBox.getItems().setAll(LaneTypeDto.values());

        MultiSelectComboBoxUtils.setupSingleSelect(
                shipmentComboBox,
                List.of(ShipmentTypeDto.values()),
                Object::toString);

        MultiSelectComboBoxUtils.setupSingleSelect(
                transportComboBox,
                List.of(TransportTypeDto.values()),
                Object::toString);

        MultiSelectComboBoxUtils.setupSingleSelect(
                laneTypeComboBox,
                List.of(LaneTypeDto.values()),
                Object::toString);
    }

    private void setupBigDecimalCell(TableColumn<LaneViewDto, BigDecimal> col) {
        col.setCellFactory(item -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : value.toPlainString());
            }
        });
    }

    private void setupPercentCell(TableColumn<LaneViewDto, BigDecimal> col) {
        col.setCellFactory(item -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText("");
                    return;
                }
                BigDecimal percent = value.multiply(BigDecimal.valueOf(100));
                setText(percent.stripTrailingZeros().toPlainString() + "%");
            }
        });
    }

    private void setupDateCell(TableColumn<LaneViewDto, LocalDate> col) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        col.setCellFactory(item -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? "" : date.format(fmt));
            }
        });
    }

    @FXML public void handleAddOrSave() {
        LaneCreateDto dto = laneForm.toDto();
        Map<String, Control> fieldMap = laneForm.fieldMap();

        var result = laneContractValidator.isValid(dto);

        if (!result.isValid()) {
            log.debug("Lane validation failed: {}", result.getErrors());
            new ValidationUIHelper(laneForm.fieldMap()).showClientErrors(result.getErrors());
            return;
        }

        try {
            CompanyDto selectedCompany = companiesTable.getSelectionModel().getSelectedItem();
            if (selectedCompany == null) {
                new ValidationUIHelper(fieldMap)
                        .showClientErrors(List.of(new ValidationError("customerName", "Please select a customer name.")));
                AlertUtils.showError("Please select a customer name!");
                return;
            }

            if (formState.isEditMode()) {
                LaneViewDto editing = formState.getEditingLane();
                LaneViewDto updated = laneClient.update(editing.id(), dto);

                int idx = lanesTable.getItems().indexOf(editing);
                if (idx >= 0) {
                    lanesTable.getItems().set(idx, updated);
                }
                formState.switchToCreate();
                laneForm.reset();

            } else {
                LaneViewDto lane = laneClient.create(selectedCompany.id(), dto);
                lanesTable.getItems().add(lane);
                laneForm.reset();
            }


        } catch (ApiValidationException ex) {
            new ValidationUIHelper(fieldMap).showBackendErrors(ex);
        }
    }

    @FXML public void handleRemove() {
        LaneViewDto selected = lanesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Nothing selected.");
            return;
        }

        boolean ok =
                AlertUtils.showConfirmation(null, "Are you sure that you want to delete this lane? " +
                        "This action cannot be undone.");

        if (!ok) return;

        try {
            laneClient.delete(selected.id());
            lanesTable.getItems().remove(selected);

            formPolicy.onDelete(formState, selected);
            laneForm.reset();
        } catch (ApiException ex) {
            AlertUtils.showError("This lane cannot be deleted because it is used in existing requests.");
            log.warn("Failed to delete lane {}: {}", selected.id(), ex.getMessage());
        }
    }

    @FXML public void handleEdit() {
        LaneViewDto selected = lanesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("No selection, please select a lane to edit.");
            return;
        }

        fillFormFromLane(selected);
        switchToEditMode(selected);
    }

    private void fillFormFromLane(LaneViewDto lane) {
        laneIdTextField.setText(lane.laneName());
        fromTextField.setText(LocationTextFormatter.format(lane.fromLocation()));
        toTextField.setText(LocationTextFormatter.format(lane.toLocation()));

        shipmentComboBox.setValue(lane.shipmentType());
        transportComboBox.setValue(lane.transportType());
        laneTypeComboBox.setValue(lane.type());

        boolean isTempTruck = lane.transportType() == TransportTypeDto.REF;
        tempTextField.setDisable(!isTempTruck);
        tempTextField.setText(isTempTruck && lane.temperature() != null
                        ? lane.temperature()
                        : ""
        );

        weightTextField.setText(
                lane.weight() != null ? lane.weight().toString() : ""
        );

        priceTextField.setText(
                lane.price() != null ? lane.price().toPlainString() : ""
        );

        fuelSurchargeTextField.setText(
                lane.fuelSurcharge() != null
                        ? lane.fuelSurcharge()
                        .multiply(BigDecimal.valueOf(100))
                        .stripTrailingZeros()
                        .toPlainString()
                        : ""
        );

        validFrom.setValue(lane.validFrom());
        validTo.setValue(lane.validTo());
    }

    private void setupResetUtils() {
        ResetUtils.resetOnChange(laneIdTextField, laneIdTextField.textProperty());
        ResetUtils.resetOnChange(fromTextField, fromTextField.textProperty());
        ResetUtils.resetOnChange(toTextField, toTextField.textProperty());
        ResetUtils.resetOnChange(tempTextField, tempTextField.textProperty());
        ResetUtils.resetOnChange(weightTextField, weightTextField.textProperty());
        ResetUtils.resetOnChange(priceTextField, priceTextField.textProperty());
        ResetUtils.resetOnChange(fuelSurchargeTextField, fuelSurchargeTextField.textProperty());
        ResetUtils.resetOnChange(shipmentComboBox, shipmentComboBox.valueProperty());
        ResetUtils.resetOnChange(transportComboBox, transportComboBox.valueProperty());
        ResetUtils.resetOnChange(validFrom, validFrom.valueProperty());
        ResetUtils.resetOnChange(validTo, validTo.valueProperty());
        ResetUtils.resetOnChange(laneTypeComboBox, laneTypeComboBox.valueProperty());
    }

    private void switchToCreateMode() {
        formState.switchToCreate();
        laneForm.reset();
    }

    private void switchToEditMode(LaneViewDto lane) {
        formPolicy.onEditRequested(formState, lane);
        fillFormFromLane(lane);
    }
}
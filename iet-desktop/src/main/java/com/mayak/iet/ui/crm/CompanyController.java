package com.mayak.iet.ui.crm;

import com.mayak.iet.integration.api.CompanyClient;
import com.mayak.iet.integration.exception.ApiException;
import com.mayak.iet.company.dto.CompanyCreateDto;
import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.ui.administration.AbstractSettingsController;
import com.mayak.iet.ui.home.HomeController;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.error.ApiErrorUtils;
import com.mayak.iet.infrastructure.common.ResetUtils;
import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@Scope
@RequiredArgsConstructor
@Slf4j
public class CompanyController extends AbstractSettingsController<CompanyDto, CompanyDto, CompanyCreateDto> {

    @FXML public TextField companyNameField;
    @FXML public Button addButton, removeButton, editButton;
    @FXML public TableView<CompanyDto> companiesTable;
    @FXML public TableColumn<CompanyDto, String> companyNameColumn;

    private final CompanyClient companyClient;

    @Getter @Setter
    private HomeController homeController;

    @FXML
    public void initialize() {
        TextUtils.allowOnlyLatin(true, companyNameField);
        companiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        companyNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));

        initValidation();
    }

    @Override
    protected Long extractId(CompanyDto view) {
        return view.id();
    }

    @Override
    protected CompanyCreateDto buildCreateDto() {
        return new CompanyCreateDto(
                companyNameField.getText().trim().toUpperCase()
        );
    }

    @Override
    protected CompanyDto buildUpdateDto(Long id) {
        return new CompanyDto(
                id,
                companyNameField.getText().trim().toUpperCase()
        );
    }

    @Override
    protected void update(CompanyDto dto) {companyClient.update(dto.id(), dto);}

    @Override
    protected void add(CompanyCreateDto dto) {
        companyClient.create(dto);
    }

    @Override
    protected void remove(CompanyDto item) {
        if (item == null) {
            AlertUtils.showError("Please select a customer to delete.");
            return;
        }

        boolean ok = AlertUtils.showConfirmation(null, "Are you sure you want to delete this customer?");
        if (!ok) return;

        try {
            companyClient.delete(item.id());
        } catch (ApiException ex) {
            AlertUtils.show(ApiErrorUtils.resolve(ex, "This company cannot be deleted because it is used in existing requests."));
        }

    }

    @Override
    protected Iterable<CompanyDto> loadAll() {
        return companyClient.findAll();
    }

    @Override
    protected void fillForm(CompanyDto view) {
        companyNameField.setText(view.name());
    }

    @Override
    protected void resetFields() {
        ResetUtils.reset(companyNameField);
    }

    @Override
    protected Map<String, Control> getFieldMap() {
        return Map.of("name", companyNameField);
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
    protected TableView<CompanyDto> getTable() {
        return companiesTable;
    }
}
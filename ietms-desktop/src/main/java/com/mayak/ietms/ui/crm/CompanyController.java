package com.mayak.ietms.ui.crm;

import com.mayak.ietms.common.util.UnicodeNormalizer;
import com.mayak.ietms.integration.api.CompanyClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.company.dto.CompanyCreateDto;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.integration.websocket.CompanyStompClient;
import com.mayak.ietms.ui.administration.AbstractSettingsController;
import com.mayak.ietms.ui.core.RequiresPermission;
import com.mayak.ietms.ui.core.ViewPermission;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.common.ResetUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * CRM screen for managing client companies.
 *
 * <p>Extends {@link AbstractSettingsController} with real-time WebSocket updates:
 * company create/update/delete events from other users are applied directly
 * to the table without a full reload.
 */
@Controller
@FxmlView("crm_company.fxml")
@Scope("prototype")
@RequiresPermission(ViewPermission.CRM)
@RequiredArgsConstructor
@Slf4j
public class CompanyController extends AbstractSettingsController<CompanyDto, CompanyDto, CompanyCreateDto> {

    @FXML
    public TextField companyNameField;

    @FXML
    public Button addButton, removeButton, editButton;

    @FXML
    public TableView<CompanyDto> companiesTable;

    @FXML
    public TableColumn<CompanyDto, String> companyNameColumn;

    private final CompanyClient companyClient;
    private final CompanyStompClient companyStompClient;

    @Getter @Setter
    private HomeController homeController;

    @FXML
    public void initialize() {
        companiesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        companyNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().name()));
        initValidation();
    }

    @Override
    public void onShow() {
        super.onShow();
        companyStompClient.connect(event -> Platform.runLater(() -> {
            if (event == null || event.getType() == null || event.getPayload() == null) return;

            switch (event.getType()) {
                case CREATED -> {
                    var items = getTable().getItems();
                    boolean exists = items.stream()
                            .anyMatch(c -> c.id().equals(event.getPayload().id()));
                    if (!exists) items.add(event.getPayload());
                }
                case UPDATED -> {
                    var items = getTable().getItems();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).id().equals(event.getPayload().id())) {
                            items.set(i, event.getPayload());
                            break;
                        }
                    }
                }
                case DELETED -> getTable().getItems()
                        .removeIf(c -> c.id().equals(event.getPayload().id()));
            }
        }));
    }

    @Override
    protected Long extractId(CompanyDto view) {
        return view.id();
    }

    @Override
    protected CompanyCreateDto buildCreateDto() {
        return new CompanyCreateDto(UnicodeNormalizer.normalize(companyNameField.getText()).toUpperCase());
    }

    @Override
    protected CompanyDto buildUpdateDto(Long id) {
        return new CompanyDto(id, UnicodeNormalizer.normalize(companyNameField.getText()).toUpperCase());
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
            AlertUtils.show(ApiErrorUtils.resolve(ex));
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

    @Override
    protected boolean canEdit() {
        return permissions != null && permissions.canViewCrm();
    }
}
package com.mayak.iet.ui.workspace.request.client;

import com.mayak.iet.common.validation.ValidationError;
import com.mayak.iet.domain.request.client.ClientRequestPolicy;
import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.extension.event.ExtensionDraftInvalidEvent;
import com.mayak.iet.integration.websocket.ExtensionStompClient;
import com.mayak.iet.lane.dto.LaneViewDto;
import com.mayak.iet.request.dto.create.BaseRequestDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.integration.api.CompanyClient;
import com.mayak.iet.integration.api.LaneClient;
import com.mayak.iet.integration.api.RequestClient;
import com.mayak.iet.integration.exception.ApiException;
import com.mayak.iet.integration.exception.ApiValidationException;
import com.mayak.iet.integration.websocket.RequestStompClient;
import com.mayak.iet.ui.component.LaneSelectorController;
import com.mayak.iet.ui.workspace.request.base.AbstractRequestController;
import com.mayak.iet.ui.workspace.request.base.ParentType;
import com.mayak.iet.ui.workspace.request.form.ClientRequestFormState;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.error.ApiErrorUtils;
import com.mayak.iet.infrastructure.assembler.ClientRequestAssembler;
import com.mayak.iet.infrastructure.common.TextUtils;
import com.mayak.iet.infrastructure.fx.AutoCompleteUtils;
import com.mayak.iet.support.state.RequestFilterState;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.infrastructure.time.DatePickerUtils;
import com.mayak.iet.infrastructure.ui.ValidationUIHelper;
import com.mayak.iet.infrastructure.window.WindowService;
import com.mayak.iet.request.validator.RequestContractValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ClientRequestsController extends AbstractRequestController {

    // ==================== FXML ====================
    @FXML private TextArea fromTextArea, toTextArea, commentsTextArea;
    @FXML private TextField idField, companyField, temperatureTextField, weightTextField, loadingMeterTextField;
    @FXML private DatePicker startDate, endDate;
    @FXML private RadioButton spotRadioButton, contractRadioButton, ftlRadioButton, ltlRadioButton;
    @FXML private ComboBox<TransportTypeDto> transportComboBox;
    @FXML private CheckBox dangerousCheckBox;
    @FXML public Button laneButton;

    private final ClientRequestPolicy requestPolicy = new ClientRequestPolicy();
    private final ClientRequestFormState requestState = new ClientRequestFormState();

    private final ToggleGroup requestTypeGroup = new ToggleGroup();
    private final ToggleGroup shipmentTypeGroup = new ToggleGroup();

    private final CompanyClient companyClient;
    private final LaneClient laneClient;
    private ValidationUIHelper validationUI;

    private final ExtensionStompClient extensionStompClient;

    @Setter
    private Long renewedRequest;

    private boolean isRendering = false;
    private boolean allowLaneLookup = true;
    private volatile boolean extensionActive = false;

    @Override
    public ParentType getParentType() {
        return ParentType.CLIENT;
    }

    public ClientRequestsController(
            RequestClient requestClient,
            WindowService windowService,
            RequestFilterState filterState,
            RequestStompClient wsClient,
            CompanyClient companyClient,
            LaneClient laneClient,
            ExtensionStompClient extensionStompClient)
    {
        super(requestClient, windowService, filterState,  wsClient);
        this.companyClient = companyClient;
        this.laneClient = laneClient;
        this.extensionStompClient = extensionStompClient;
    }

    @FXML
    public void initialize() {
        spotRadioButton.setToggleGroup(requestTypeGroup);
        contractRadioButton.setToggleGroup(requestTypeGroup);

        ftlRadioButton.setToggleGroup(shipmentTypeGroup);
        ltlRadioButton.setToggleGroup(shipmentTypeGroup);

        Set<String> companies = companyClient.findAll()
                .stream()
                .map(CompanyDto::name)
                .collect(Collectors.toSet());

        setupFormFields();
        bindState();
        onCompanyConfirmed();
        AutoCompleteUtils.setupAutoCompletion(companyField, companies);

        validationUI = new ValidationUIHelper(Map.ofEntries(
                Map.entry("requestType", spotRadioButton),
                Map.entry("fromLocations", fromTextArea),
                Map.entry("toLocations", toTextArea),
                Map.entry("customerReference", idField),
                Map.entry("customerName", companyField),
                Map.entry("lane", laneButton),
                Map.entry("startDate", startDate),
                Map.entry("endDate", endDate),
                Map.entry("shipmentType", ftlRadioButton),
                Map.entry("transportType", transportComboBox),
                Map.entry("temperature", temperatureTextField),
                Map.entry("weight", weightTextField),
                Map.entry("loadingMeter", loadingMeterTextField),
                Map.entry("comments", commentsTextArea)));

        validationUI.bindResetOnChange();
        requestState.reset();
        render();
    }

    @Override
    public void onShow() {
        super.onShow();

        extensionActive = true;
        extensionStompClient.connect(event ->
                Platform.runLater(() -> handleExtensionEvent(event)));

    }

    @Override
    public void onHide() {
        super.onHide();
        extensionActive = false;
        extensionStompClient.disconnect();
    }

    private void setupFormFields() {
        TextUtils.allowOnlyLatin(true, fromTextArea, toTextArea, companyField, idField, weightTextField, loadingMeterTextField);
        TextUtils.allowOnlyLatin(false, commentsTextArea);

        TextUtils.setupEnumComboBox(transportComboBox, TransportTypeDto.values(), TransportTypeDto::getLabel);
        setupListeners();
        DatePickerUtils.setupDatePickers(startDate, endDate, requestState::getLaneValidFrom);
    }

    private void setupListeners() {
        // --- request type ---
        requestTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            boolean isContract = newToggle == contractRadioButton;
            requestPolicy.onRequestTypeChanged(requestState, isContract);

            if (!isContract) {
                validationUI.clearError("customerName");
                validationUI.clearError("lane");
            }
            render();
        });

        // --- shipment type ---
        shipmentTypeGroup.selectedToggleProperty().addListener((obs, o, n) -> {
            if (n == null) return;

            ShipmentTypeDto type = n == ltlRadioButton
                    ? ShipmentTypeDto.LTL
                    : ShipmentTypeDto.FTL;

            requestPolicy.onShipmentTypeChanged(requestState, type);
            render();
        });

        // --- transport type ---
        transportComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null) return;
            requestPolicy.onTransportTypeChanged(requestState, n);
            render();
        });

        companyField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!requestState.isContract()) {
                return;
            }

            if (requestState.hasLane()
                    && oldVal != null
                    && !Objects.equals(oldVal, newVal)) {

                requestPolicy.onCompanyChanged(requestState);
                render();
            }
        });
    }

    private void onCompanyConfirmed() {
        if (!allowLaneLookup) return;
        if (!requestState.isContract()) return;

        String value = TextUtils.safeTrim(companyField.getText());
        if (value == null) return;

        try {
            companyClient.findByName(value)
                    .ifPresentOrElse(
                            this::openLaneSelector,
                            () -> AlertUtils.showError("Company not found.")
                    );
        } catch (ApiException ex) {
            log.warn("Company lookup failed", ex);
            AlertUtils.show(ApiErrorUtils.resolve(ex, "Failed to find company."));
        }
    }

    private void openLaneSelector(CompanyDto company) {
        List<LaneViewDto> lanes = laneClient.findByCompany(company.id());
        if (lanes.isEmpty()) {
            AlertUtils.showError("No available lanes for this customerName.");
            return;
        }

        LaneSelectorController ctrl = windowService.openModalAndWait(
                View.LANES.getPath(),
                LaneSelectorController.class,
                controller -> {
                    controller.setLanes(lanes);
                    controller.onShown();
                },
                "Actual contract lanes",
                null
        );

        LaneViewDto lane = ctrl.getSelectedLane();
        if (lane == null) return;

        requestPolicy.onLaneSelected(requestState, lane);
        validationUI.clearError("lane");
        render();
    }

    private void render() {
        isRendering = true;
        try {
            renderData();
            renderState();
        } finally {
            isRendering = false;
        }
    }

    private void renderData() {
        // --- request type ---
        if (requestState.getContract() == null) {
            requestTypeGroup.selectToggle(null);
        } else if (requestState.isContract()) {
            contractRadioButton.setSelected(true);
        } else {
            spotRadioButton.setSelected(true);
        }

        // --- shipment type ---
        if (requestState.getShipmentType() == null) {
            shipmentTypeGroup.selectToggle(null);
        } else if (requestState.getShipmentType() == ShipmentTypeDto.FTL) {
            ftlRadioButton.setSelected(true);
        } else {
            ltlRadioButton.setSelected(true);
        }

        transportComboBox.setValue(requestState.getTransportType());

        fromTextArea.setText(Objects.toString(requestState.getFrom(), ""));
        toTextArea.setText(Objects.toString(requestState.getTo(), ""));
        idField.setText(Objects.toString(requestState.getCustomerReference(), ""));
        companyField.setText(Objects.toString(requestState.getCompanyName(), ""));

        weightTextField.setText(Objects.toString(requestState.getWeight(), ""));
        loadingMeterTextField.setText(requestState.isLtl() ? Objects.toString(requestState.getLoadingMeter(), "") : "");
        temperatureTextField.setText(requestState.isRef() ? Objects.toString(requestState.getTemperature(), "") : "");

        dangerousCheckBox.setSelected(Boolean.TRUE.equals(requestState.getDangerous()));
        commentsTextArea.setText(Objects.toString(requestState.getComments(), ""));

        startDate.setValue(requestState.getStartDate());
        endDate.setValue(requestState.getEndDate());

    }

    private void renderState() {
        boolean showLaneButton = requestState.isContract();

        laneButton.setDisable(!showLaneButton);

        boolean hasLane = requestState.hasLane();

        if (hasLane) {
            laneButton.setText("lane ✓");
        } else {
            laneButton.setText("lane");
        }

        // --- derived fields ---
        fromTextArea.setEditable(!hasLane);
        toTextArea.setEditable(!hasLane);

        // --- shipment type ---
        ftlRadioButton.setDisable(hasLane);
        ltlRadioButton.setDisable(hasLane);

        // --- transport ---
        transportComboBox.setDisable(hasLane);

        // --- LTL ---
        loadingMeterTextField.setDisable(!requestState.isLtl() || hasLane);

        //---ADR---
        dangerousCheckBox.setDisable(hasLane);

        // --- REF ---
        temperatureTextField.setDisable(!requestState.isRef() || hasLane);

        //---weight---
        weightTextField.setDisable(hasLane);
    }

    // ==================== FORM SUBMIT ====================
    @FXML private void submitRequestForm() {
        BaseRequestDto dto = ClientRequestAssembler.build(requestState);

        normalizeCompany(dto);
        var validator = new RequestContractValidator();
        var validationResult = validator.isValid(dto);

        if (!validationResult.isValid()) {
            validationUI.showClientErrors(validationResult.getErrors());
            return;
        }

        try {
            requestClient.create(dto);
            showEmptyMessage(false);

            Optional.ofNullable(renewedRequest).ifPresent(r -> {
                try {
                    requestClient.delete(r);
                    requestItems.removeIf(existing -> existing.id().equals(r));
                } catch (Exception e) {
                    log.warn("Could not delete renewed request: {}", e.getMessage());
                } finally {
                    renewedRequest = null;
                }
            });

            resetFormFields();
            resetPagination();
            loadNextPage();
        } catch (ApiValidationException e) {
            validationUI.showBackendErrors(e);

        } catch (ApiException e) {
            log.error("Request creation failed", e);
            AlertUtils.show(ApiErrorUtils.resolve(e, "Failed to create request."));

        }
    }

    @Override
    public void fillFormWithRequest(RequestDetailsDto request) {
        allowLaneLookup = false;
        try {
            requestPolicy.applyRequest(requestState, request);
            renewedRequest = null;
            render();
        } finally {
            allowLaneLookup = true;
        }
    }

    private void resetFormFields() {
        requestState.reset();
        render();
    }

    @Override
    protected boolean allowDuplicateHotkey() {
        return true;
    }

    @Override
    protected boolean supportsFilterHotkey() { return true; }

    private void normalizeCompany(BaseRequestDto dto) {
        if (dto == null) return;

        String name = TextUtils.safeTrim(dto.getCustomerName());

        if (name == null) {
            dto.setCustomerName(null);
            return;
        }

        dto.setCustomerName(name);
    }

    private void bindState() {
        fromTextArea.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setFrom(n);
        });

        toTextArea.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setTo(n);
        });

        idField.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setCustomerReference(n);
        });

        companyField.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setCompanyName(n);
        });

        startDate.valueProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setStartDate(n);
        });

        endDate.valueProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setEndDate(n);
        });

        temperatureTextField.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setTemperature(n);
        });

        dangerousCheckBox.selectedProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setDangerous(n);
        });

        weightTextField.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setWeight(n);
        });

        loadingMeterTextField.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setLoadingMeter(n);
        });

        commentsTextArea.textProperty().addListener((obs, o, n) -> {
            if (isRendering) return;
            requestState.setComments(n);
        });

    }

    private void handleExtensionEvent(ExtensionDraftInvalidEvent event) {
        if (!extensionActive || event == null) return;

        var resp = event.payload();
        if (resp == null || resp.intent() == null) {
            log.warn("Extension event without draft payload");
            return;
        }

        requestPolicy.applyDraft(requestState, resp.intent());
        render();

        validationUI.showClientErrors(
                resp.errors().entrySet().stream()
                        .flatMap(e -> e.getValue().stream()
                                .map(msg -> new ValidationError(e.getKey(), msg)))
                        .toList()
        );
    }

    @FXML
    public void handleLaneSelect() {
        if (!requestState.isContract()) return;

        String company = TextUtils.safeTrim(requestState.getCompanyName());
        if (company == null) {
            AlertUtils.showError("Select company first");
            return;
        }

        try {
            companyClient.findByName(company)
                    .ifPresentOrElse(
                            this::openLaneSelector,
                            () -> AlertUtils.showError("Company not found.")
                    );
        } catch (ApiException ex) {
            log.warn("Company lookup failed", ex);
            AlertUtils.show(ApiErrorUtils.resolve(ex, "Failed to lookup company."));
        }
    }
}
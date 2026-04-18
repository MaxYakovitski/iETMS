package com.mayak.ietms.ui.workspace.request.client;

import com.mayak.ietms.common.util.UnicodeNormalizer;
import com.mayak.ietms.domain.request.client.ClientRequestPolicy;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.infrastructure.fx.CompanyEventHandler;
import com.mayak.ietms.integration.websocket.CompanyStompClient;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.integration.api.CompanyClient;
import com.mayak.ietms.integration.api.LaneClient;
import com.mayak.ietms.integration.api.RequestClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.ApiValidationException;
import com.mayak.ietms.integration.websocket.RequestStompClient;
import com.mayak.ietms.ui.workspace.request.base.AbstractRequestController;
import com.mayak.ietms.ui.workspace.request.base.ParentType;
import com.mayak.ietms.ui.workspace.request.form.ClientRequestFormState;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.fx.AutoCompleteUtils;
import com.mayak.ietms.support.state.RequestFilterState;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Set<String> companySuggestions = ConcurrentHashMap.newKeySet();

    private final CompanyClient companyClient;
    private final LaneClient laneClient;
    private ValidationUIHelper validationUI;

    private final CompanyStompClient companyStompClient;

    @Setter
    private Long renewedRequest;

    private ClientCompanyLaneCoordinator laneCoordinator;
    private ClientRequestSubmitService submitService;

    private Runnable companyWsUnsubscribe;

    private boolean isRendering = false;
    private boolean allowLaneLookup = true;

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
            CompanyStompClient companyStompClient)
    {
        super(requestClient, windowService, filterState,  wsClient);
        this.companyClient = companyClient;
        this.laneClient = laneClient;
        this.companyStompClient = companyStompClient;
    }

    @FXML
    public void initialize() {
        spotRadioButton.setToggleGroup(requestTypeGroup);
        contractRadioButton.setToggleGroup(requestTypeGroup);

        ftlRadioButton.setToggleGroup(shipmentTypeGroup);
        ltlRadioButton.setToggleGroup(shipmentTypeGroup);

        setupFormFields();
        bindState();

        AutoCompleteUtils.setupAutoCompletion(companyField, companySuggestions);

        CompletableFuture.supplyAsync(() ->
                        companyClient.findAll()
                                .stream()
                                .map(CompanyDto::name)
                                .collect(Collectors.toSet()))
                .thenAccept(result ->
                        Platform.runLater(() -> companySuggestions.addAll(result)))
                .exceptionally(ex -> {
                    log.warn("Company autocomplete preload failed", ex);
                    return null;
                });

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

        laneCoordinator = new ClientCompanyLaneCoordinator(
                companyClient,
                laneClient,
                windowService,
                requestPolicy,
                requestState,
                validationUI,
                this::render,
                this::setupDatePickersForCurrentState,
                () -> allowLaneLookup,
                () -> companyField.getText()
        );

        submitService = new ClientRequestSubmitService();
    }

    @Override
    public void onShow() {
        super.onShow();
        companyWsUnsubscribe = companyStompClient.connect(event ->
                Platform.runLater(() -> CompanyEventHandler.apply(event, companySuggestions)));
    }

    @Override
    public void onHide() {
        super.onHide();
        if (companyWsUnsubscribe != null) {
            companyWsUnsubscribe.run();
            companyWsUnsubscribe = null;
        }
    }

    private void setupFormFields() {
        TextUtils.allowOnlyLatin(fromTextArea, toTextArea, idField, weightTextField, loadingMeterTextField);
        TextUtils.setupEnumComboBox(transportComboBox, TransportTypeDto.values(), TransportTypeDto::getLabel);
        setupListeners();
        setupDatePickersForCurrentState();
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
            setupDatePickersForCurrentState();
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

        companyField.setOnAction(e -> laneCoordinator.onCompanyConfirmed());
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

    private void setupDatePickersForCurrentState() {
        if (requestState.hasLane()) {
            DatePickerUtils.setupLaneDatePickers(startDate, endDate, requestState::getLaneValidFrom, requestState::getLaneValidTo);
        } else {
            DatePickerUtils.setupDatePickers(startDate, endDate);
        }
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
        SubmitResult result = submitService.prepare(requestState);

        if (!result.isValid()) {
            validationUI.showClientErrors(result.validation().getErrors());
            return;
        }

        BaseRequestDto dto = result.dto();

        try {
            requestClient.create(dto);
            showEmptyMessage(false);

            Optional.ofNullable(renewedRequest).ifPresent(r -> {
                try {
                    requestClient.delete(r);
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
            setupDatePickersForCurrentState();
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
            requestState.setCompanyName(UnicodeNormalizer.normalize(n));
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

    @FXML
    public void handleLaneSelect() {
        laneCoordinator.onCompanyConfirmed();
    }
}
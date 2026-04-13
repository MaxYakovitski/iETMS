package com.mayak.ietms.ui.workspace.planner.controller;

import com.mayak.ietms.domain.planner.service.PlannerDataService;
import com.mayak.ietms.domain.planner.service.PlannerSelectionService;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.integration.api.CompanyClient;
import com.mayak.ietms.integration.api.ShipmentClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.domain.planner.model.ShipmentContext;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentCancelReasonDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.support.validation.TransportOrderValidator;
import com.mayak.ietms.ui.workspace.planner.handler.PlannerListManager;
import com.mayak.ietms.ui.workspace.planner.handler.PlannerRealtimeHandler;
import com.mayak.ietms.ui.workspace.planner.presenter.PlannerDetailsPresenter;
import com.mayak.ietms.ui.workspace.planner.presenter.TransportDetailsInput;
import com.mayak.ietms.ui.workspace.planner.state.PlannerState;
import com.mayak.ietms.ui.workspace.planner.view.PlannerDateView;
import com.mayak.ietms.ui.workspace.planner.view.TimelineToggleButton;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.UserPermissions;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.workspace.planner.base.ShipmentCell;
import com.mayak.ietms.ui.workspace.planner.enums.ActiveTab;
import com.mayak.ietms.ui.workspace.planner.form.ShipmentTransportFormState;
import com.mayak.ietms.ui.workspace.planner.policy.PlannerStatusEditPolicy;
import com.mayak.ietms.ui.workspace.planner.view.PlannerCalendarView;
import com.mayak.ietms.ui.workspace.request.item.RefuseReasonController;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.fx.AutoCompleteUtils;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.infrastructure.time.TimeSpinnerUtils;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.support.validation.StatusDateTimeValidator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PlannerController implements SecuredView, ViewLifecycle {

    @FXML public PlannerCalendarView calendarView;
    @FXML public PlannerDateView dateView;
    @FXML public Label shipmentNumber, shipmentNumberTransport, dispatcher, shipmentsEmptyMessageLabel, transportsEmptyLabel;
    @FXML public Button myShipments, myTransports, cancelButton, submitButton;
    @FXML public VBox shipmentsContentContainer, transportsContentContainer, shipmentDetailsContainer, shipmentTimelineContainer, transportDetailsContainer, transportTimelineContainer;
    @FXML public HBox shipmentsView, transportsView, buttonsContainer;
    @FXML public StackPane shipmentsLoadingOverlay, transportsLoadingOverlay;

    @FXML public TimelineToggleButton showTimeStampsButton;
    @Getter
    @FXML public TextField carrierField, licensePlateField, transportOrder;
    @Getter
    @FXML public DatePicker dateAndTime;
    @Getter
    @FXML public Spinner<LocalTime> timeSpinner;
    @FXML public TextArea commentsTextArea;
    @FXML public ComboBox<ShipmentStatusDto> shipmentStatusComboBox;
    @FXML private ListView<ShipmentListItemDto> shipmentsListView, transportsListView;

    /* ================= Dependencies ================= */
    private final PlannerStatusEditPolicy statusEditPolicy;
    private final PlannerDataService dataService;
    private final PlannerSelectionService selectionService;
    private final PlannerDetailsPresenter detailsPresenter;

    private final WindowService windowService;

    private final CompanyClient companyClient;
    private final ShipmentClient shipmentClient;
    private final PlannerRealtimeHandler realtimeHandler;
    private final PlannerListManager listManager;

    private Runnable companyWsUnsubscribe;

    /* ================= State ================= */
    private final PlannerState state = new PlannerState();
    private final ShipmentTransportFormState formState = new ShipmentTransportFormState();
    private final Set<String> companySuggestions = ConcurrentHashMap.newKeySet();
    private ValidationUIHelper validationUI;
    private UserPermissions permissions;
    private boolean initializingForm = false;
    private boolean restoringSelection = false;

    @Getter
    private UserResponseDto loggedInUser;

    /* ================= UI Runtime ================= */
    private final ObservableList<ShipmentListItemDto> shipmentItems = FXCollections.observableArrayList();
    private final ObservableList<ShipmentListItemDto> transportItems = FXCollections.observableArrayList();


    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
    }

    @Override
    public void onShow() {
        boolean canViewShipments = permissions.canViewMyShipments();
        applyPermissions(canViewShipments);
        if (canViewShipments) {
            state.setActiveTab(ActiveTab.MY_SHIPMENTS);
            setActive(myShipments, myTransports);
            calendarView.resetToToday();
        } else {
            state.setActiveTab(ActiveTab.MY_TRANSPORTS);
            loadTransports();
        }
        initShipmentRealtimeUpdates();
    }

    @Override
    public void onHide() {
        if (companyWsUnsubscribe != null) {
            companyWsUnsubscribe.run();
            companyWsUnsubscribe = null;
        }
    }

    /* ================= Realtime Updates ================= */
    private void initShipmentRealtimeUpdates() {
        companyWsUnsubscribe = realtimeHandler.init(
                companySuggestions,
                () -> {
                    if (permissions.canViewMyShipments()) loadShipments(state.getSelectedDate());
                    loadTransports();
                },
                listManager::invalidateShipment);
    }

    @FXML
    private void initialize() {
        TextUtils.allowOnlyLatin(true, carrierField, licensePlateField, transportOrder);
        validationUI = new ValidationUIHelper(
                Map.of(
                        "statusDate", dateAndTime,
                        "statusTime", timeSpinner,
                        "carrier", carrierField,
                        "licensePlate", licensePlateField
                )
        );
        validationUI.bindResetOnChange();
        calendarView.selectedDateProperty()
                .addListener((obs, oldD, newD) -> {
                    if (newD == null) return;
                    clearSelection();
                    loadShipments(newD);
                });
        statusEditPolicy.configure(shipmentStatusComboBox);

        shipmentsListView.setItems(shipmentItems);
        transportsListView.setItems(transportItems);

        setupList(shipmentsListView, ActiveTab.MY_SHIPMENTS);
        setupList(transportsListView, ActiveTab.MY_TRANSPORTS);

        setupDirtyListeners();

        CompletableFuture.supplyAsync(() ->
                        companyClient.findAll().stream()
                                .map(CompanyDto::name)
                                .collect(Collectors.toSet()))
                .thenAccept(result -> Platform.runLater(() -> companySuggestions.addAll(result)))
                .exceptionally(ex -> { log.warn("Company preload failed", ex); return null; });

        AutoCompleteUtils.setupAutoCompletion(carrierField, companySuggestions);
        DatePickerUtils.setupDatePicker(dateAndTime);
        TimeSpinnerUtils.setupTimeSpinner(timeSpinner, 15);

        listManager.init(
                shipmentItems,
                transportItems,
                state,
                this::showMyShipmentDetails,
                this::showMyTransportDetails);
    }

    private void setupList(ListView<ShipmentListItemDto> list, ActiveTab activeTab) {
        list.setCellFactory(lv -> {
            ShipmentCell cell = new ShipmentCell(windowService);
            cell.setActiveTab(activeTab);
            return cell;
        });

        list.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem == null) return;
                    if (restoringSelection) return;
                    selectShipment(newItem);
                });
    }

    private void setupDirtyListeners() {
        carrierField.textProperty()
                .addListener((obs, oldVal, newVal) -> markDirtyIfChanged());
        commentsTextArea.textProperty()
                .addListener((obs, oldVal, newVal) -> markDirtyIfChanged());
        licensePlateField.textProperty()
                .addListener((obs, o, n) -> markDirtyIfChanged());
        transportOrder.textProperty()
                .addListener((obs, o, n) -> markDirtyIfChanged());
        shipmentStatusComboBox.valueProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (state.getSelectedShipment() == null) return;

                    statusEditPolicy.apply(newVal, dateAndTime, timeSpinner);
                    markDirtyIfChanged();
                });
    }

    /* ================= Form State ================= */
    private void markDirtyIfChanged() {
        if (initializingForm) return;
        if (state.getSelectedShipment() == null) return;

        formState.updateCurrent(
                carrierField.getText(),
                commentsTextArea.getText(),
                licensePlateField.getText(),
                transportOrder.getText(),
                shipmentStatusComboBox.getValue());

        updateSubmitState();
    }

    private void updateSubmitState() {
        submitButton.setDisable(!formState.isDirty());
    }

    /* ================= Event Handlers ================= */
    @FXML
    public void submitShipmentUpdate() {
        if (!formState.isDirty() || state.getSelectedShipment() == null) return;

        ShipmentUpdateDto updateDto = buildUpdateDto();
        if (updateDto == null) return;

        try {
            ShipmentListItemDto fresh = shipmentClient.update(updateDto);
            listManager.applyShipmentUpdate(fresh);
        } catch (ApiException e) {
            log.warn("Shipment update rejected by backend", e);
            AlertUtils.show(ApiErrorUtils.resolve(e, "Shipment update failed."));
        }
    }

    /* ================= Update / Mutation ================= */
    private ShipmentUpdateDto buildUpdateDto() {
        if (state.getSelectedShipment() == null) return null;

        formState.updateCurrent(
                carrierField.getText(),
                commentsTextArea.getText(),
                licensePlateField.getText(),
                transportOrder.getText(),
                shipmentStatusComboBox.getValue()
        );

        LocalDateTime statusAt = null;

        var transportResult = new TransportOrderValidator().isValid(this);
        if (!transportResult.isValid()) {
            validationUI.showClientErrors(transportResult.getErrors());
            return null;
        }

        if (shipmentStatusComboBox.getValue() != null) {
            var result = new StatusDateTimeValidator().isValid(this);
            if (!result.isValid()) {
                validationUI.showClientErrors(result.getErrors());
                return null;
            }
            statusAt = LocalDateTime.of(dateAndTime.getValue(), timeSpinner.getValue());
        }

        return formState.toUpdateDto(state.getSelectedShipment().id(), statusAt);
    }

    @FXML
    public void onMyShipmentsHandle() {
        if (!permissions.canViewMyShipments()) return;
        switchTab(ActiveTab.MY_SHIPMENTS);
    }

    @FXML
    public void onMyTransportsHandle() {
        switchTab(ActiveTab.MY_TRANSPORTS);
        loadTransports();
    }

    @FXML
    public void handleShowHistory() {
        boolean nowVisible = showTimeStampsButton.toggle();
        transportTimelineContainer.setVisible(nowVisible);
        transportTimelineContainer.setManaged(nowVisible);
    }

    @FXML
    public void handleCancel() {
        if (state.getSelectedShipment() == null) return;

        boolean confirmed = AlertUtils.showConfirmation(
                "Cancellation",
                "Are you sure you want to cancel shipment " + state.getSelectedShipment().id() + "?");
        if (!confirmed) return;

        String fxmlPath = View.REFUSE_REASON.getPath();

        windowService.openModalWindow(
                fxmlPath,
                RefuseReasonController.class,
                controller -> {

                    @SuppressWarnings("unchecked")
                    RefuseReasonController<ShipmentCancelReasonDto> c =
                            (RefuseReasonController<ShipmentCancelReasonDto>) controller;

                    c.init(
                            List.of(ShipmentCancelReasonDto.values()),
                            ShipmentCancelReasonDto::name,
                            reason -> {
                                try {
                                    shipmentClient.cancel(state.getSelectedShipment().id(), reason);
                                    reloadAfterMutation();
                                } catch (ApiException e) {
                                    log.warn("Failed to cancel shipment {}", state.getSelectedShipment().id(), e);
                                    AlertUtils.show(ApiErrorUtils.resolve(e, "Shipment cancellation failed."));
                                }
                            }
                    );
                },
                "Reason",
                null,
                null
        );
    }

    /* ================= Core Orchestration ================= */
    private void loadShipments(LocalDate date) {
        state.setSelectedDate(date);

        CompletableFuture.supplyAsync(() -> dataService.loadMyShipments(date))
                .thenAccept(result -> Platform.runLater(() -> {
                    shipmentItems.setAll(result);

                    if (state.getSelectedShipment() != null) {
                        Long selectedId = state.getSelectedShipment().id();
                        IntStream.range(0, result.size())
                                .filter(i -> Objects.equals(result.get(i).id(), selectedId))
                                .findFirst()
                                .ifPresent(shipmentsListView.getSelectionModel()::select);
                    }

                    state.setHasMyShipments(!result.isEmpty());
                    shipmentsEmptyMessageLabel.setVisible(result.isEmpty());
                    shipmentsEmptyMessageLabel.setManaged(result.isEmpty());

                    shipmentsLoadingOverlay.setVisible(false);
                    shipmentsLoadingOverlay.setManaged(false);
                    shipmentsContentContainer.setVisible(true);
                    shipmentsContentContainer.setManaged(true);

                }))
                .exceptionally(ex -> {
                    log.warn("Failed to load shipments for {}", date, ex);
                    return null;
                });
    }

    private void loadTransports() {
        CompletableFuture.supplyAsync(dataService::loadMyTransports)
                .thenAccept(result -> Platform.runLater(() -> {
                    transportItems.setAll(result);

                    if (state.getSelectedShipment() != null) {
                        Long selectedId = state.getSelectedShipment().id();
                        result.stream()
                                .filter(s -> Objects.equals(s.id(), selectedId))
                                .findFirst()
                                .ifPresent(fresh -> {
                                    if (fresh.status() == ShipmentStatusDto.CANCELED) {
                                        state.setSelectedShipment(fresh);
                                        showMyTransportDetails(fresh);
                                    }
                                    restoringSelection = true;
                                    IntStream.range(0, result.size())
                                            .filter(i -> Objects.equals(result.get(i).id(), selectedId))
                                            .findFirst()
                                            .ifPresent(transportsListView.getSelectionModel()::select);
                                    restoringSelection = false;
                                });
                    }

                    state.setHasMyTransports(!result.isEmpty());
                    transportsEmptyLabel.setVisible(result.isEmpty());
                    transportsEmptyLabel.setManaged(result.isEmpty());

                    transportsLoadingOverlay.setVisible(false);
                    transportsLoadingOverlay.setManaged(false);
                    transportsContentContainer.setVisible(true);
                    transportsContentContainer.setManaged(true);
                }))
                .exceptionally(ex -> {
                    log.warn("Failed to load transports", ex);
                    return null;
                });
    }

    private void reloadAfterMutation() {
        if (permissions.canViewMyShipments()) {
            loadShipments(state.getSelectedDate());
        }
        loadTransports();
    }

    /* ================= Selection & Tabs ================= */
    public void selectShipment(ShipmentListItemDto dto) {
        state.setSelectedShipment(dto);

        if (selectionService.isShipmentTab(state)) {
            showMyShipmentDetails(dto);
        } else if (selectionService.isTransportTab(state)) {
            showMyTransportDetails(dto);
        }
    }

    private void switchTab(ActiveTab tab) {
        if (state.getActiveTab() != tab) {
            clearSelection();
        }

        state.setActiveTab(tab);

        switch (tab) {
            case MY_SHIPMENTS -> {
                setActive(myShipments, myTransports);
                shipmentsView.setVisible(true);
                shipmentsView.setManaged(true);
                transportsView.setVisible(false);
                transportsView.setManaged(false);
                calendarView.resetToToday();
            }
            case MY_TRANSPORTS -> {
                setActive(myTransports, myShipments);
                transportsView.setVisible(true);
                transportsView.setManaged(true);
                shipmentsView.setVisible(false);
                shipmentsView.setManaged(false);
            }
        }
    }

    /* ================= Details ================= */
    private void showMyShipmentDetails(ShipmentListItemDto dto) {
        detailsPresenter.showShipmentDetails(
                dto,
                shipmentNumber,
                dispatcher,
                shipmentDetailsContainer,
                shipmentTimelineContainer,
                cancelButton
        );
    }

    private void showMyTransportDetails(ShipmentListItemDto dto) {
        transportTimelineContainer.setVisible(false);
        transportTimelineContainer.setManaged(false);
        showTimeStampsButton.reset();

        initializingForm = true;
        formState.reset();
        submitButton.setDisable(true);

        ShipmentContext ctx = selectionService.buildContext(dto, state.getSelectedDate());

        detailsPresenter.showTransportDetails(
                new TransportDetailsInput(
                        dto, ctx, shipmentNumberTransport, transportTimelineContainer,
                        transportDetailsContainer, carrierField, commentsTextArea,
                        licensePlateField, transportOrder, shipmentStatusComboBox,
                        dateAndTime, timeSpinner)
        );

        formState.bindTo(dto);
        initializingForm = false;
        updateSubmitState();
    }

    private void hideDetailsCompletely() {
        detailsPresenter.hideAll(shipmentDetailsContainer, shipmentTimelineContainer, transportDetailsContainer, transportTimelineContainer, cancelButton);
    }

    private void clearSelection() {
        state.setSelectedShipment(null);
        shipmentsListView.getSelectionModel().clearSelection();
        transportsListView.getSelectionModel().clearSelection();
        hideDetailsCompletely();

        carrierField.clear();
        commentsTextArea.clear();
        licensePlateField.clear();
        transportOrder.clear();
        shipmentStatusComboBox.setValue(null);
        statusEditPolicy.reset(dateAndTime, timeSpinner);
        formState.reset();
        updateSubmitState();
        showTimeStampsButton.reset();
    }

    /* ================= UI Helpers ================= */
    private void setActive(Button active, Button inactive) {
        if (!active.getStyleClass().contains("active-tab")) {
            active.getStyleClass().add("active-tab");
        }
        inactive.getStyleClass().remove("active-tab");
    }

    private void applyPermissions(boolean canViewShipments) {
        buttonsContainer.setVisible(canViewShipments);
        buttonsContainer.setManaged(canViewShipments);

        shipmentsView.setVisible(canViewShipments);
        shipmentsView.setManaged(canViewShipments);

        transportsView.setVisible(!canViewShipments);
        transportsView.setManaged(!canViewShipments);
    }
}
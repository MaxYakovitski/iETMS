package com.mayak.ietms.ui.workspace.planner.controller;

import com.mayak.ietms.domain.planner.service.PlannerDataService;
import com.mayak.ietms.domain.planner.service.PlannerSelectionService;
import com.mayak.ietms.domain.planner.service.ShipmentSortingService;
import com.mayak.ietms.infrastructure.fx.CompanyEventHandler;
import com.mayak.ietms.integration.websocket.CompanyStompClient;
import com.mayak.ietms.request.dto.event.ShipmentEventDto;
import com.mayak.ietms.shipment.dto.enums.TransportEventType;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.toast.ToastService;
import com.mayak.ietms.integration.api.CompanyClient;
import com.mayak.ietms.integration.api.ShipmentClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.domain.planner.model.ShipmentContext;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentCancelReasonDto;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.ui.workspace.planner.presenter.PlannerDetailsPresenter;
import com.mayak.ietms.ui.workspace.planner.state.PlannerState;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.integration.websocket.ShipmentStompClient;
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
import com.mayak.ietms.infrastructure.window.WindowService;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PlannerController implements SecuredView, ViewLifecycle {

    @FXML public Label yearLabel, monthLabel, shipmentNumber, shipmentNumberTransport, dispatcher, emptyMessageLabel;
    @FXML public Button leftButton, rightButton, myShipments, myTransports, cancelButton, submitButton;
    @FXML public VBox contentContainer, shipmentDetailsContainer, timelineContainer, transportDetailsContainer, myShipmentsContainer, myTransportsContainer,
                      toLoadContainer, toDropContainer;
    @FXML public HBox buttonsContainer;
    @FXML public StackPane loadingOverlay;

    @Getter
    @FXML public DatePicker dateAndTime;
    @Getter
    @FXML public Spinner<LocalTime> timeSpinner;

    @FXML private StackPane calendarContainer, detailsStack;
    @FXML public TextArea commentsTextArea;
    @FXML public TextField carrierField, licensePlateField, transportOrder;
    @FXML public ComboBox<ShipmentStatusDto> shipmentStatusComboBox;
    @FXML private ListView<ShipmentListItemDto> toLoadListView, toDropListView, shipmentsListView;

    /* ================= Dependencies ================= */
    private final PlannerStatusEditPolicy statusEditPolicy;
    private final PlannerDataService dataService;
    private final PlannerSelectionService selectionService;
    private final PlannerDetailsPresenter detailsPresenter;
    private final ShipmentSortingService sortingService;

    private final CompanyClient companyClient;
    private final ShipmentClient shipmentClient;
    private final CompanyStompClient companyStompClient;
    private final ShipmentStompClient shipmentStompClient;
    private final WindowService windowService;

    private Runnable companyWsUnsubscribe;

    /* ================= State ================= */
    private final PlannerState state = new PlannerState();
    private final ShipmentTransportFormState formState = new ShipmentTransportFormState();
    private final Set<String> companySuggestions = ConcurrentHashMap.newKeySet();
    private ValidationUIHelper validationUI;
    private UserPermissions permissions;
    private boolean initializingForm = false;

    @Getter
    private UserResponseDto loggedInUser;

    /* ================= UI Runtime ================= */
    private PlannerCalendarView calendarView;

    private final ObservableList<ShipmentListItemDto> toLoadItems = FXCollections.observableArrayList();
    private final ObservableList<ShipmentListItemDto> toDropItems = FXCollections.observableArrayList();
    private final ObservableList<ShipmentListItemDto> shipmentsItems = FXCollections.observableArrayList();

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
    }

    @Override
    public void onShow() {
        applyPermissions();
        loadForDate(state.getSelectedDate());
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
        shipmentStompClient.connect(event -> Platform.runLater(() -> {
            handleShipmentEvent(event);
            handleShipmentUserEvent(event);
        }));
        companyWsUnsubscribe = companyStompClient.connect(event ->
                Platform.runLater(() -> CompanyEventHandler.apply(event, companySuggestions)));
    }

    private void handleShipmentEvent(ShipmentEvent<ShipmentEventDto> event) {
        if (event == null) return;

        switch (event.getType()) {
            case STATUS_CHANGED -> loadForDate(state.getSelectedDate());
            case UPDATED -> invalidateShipment(event.getShipmentId());
        }
    }

    private void handleShipmentUserEvent(ShipmentEvent<ShipmentEventDto> event) {
        if (event == null || event.getPayload() == null) return;
        if (event.getType() != ShipmentEvent.EventType.STATUS_CHANGED) return;

        if (ShipmentStatusDto.CANCELED.name().equals(event.getPayload().status())) {
            ToastService.showInfo(
                    windowService.getPrimaryStage(),
                    "Shipment canceled",
                    "Shipment #" + event.getShipmentId() + " has been canceled");
        }
    }

    @FXML
    private void initialize() {
        TextUtils.allowOnlyLatin(true, carrierField, licensePlateField, transportOrder);
        validationUI = new ValidationUIHelper(
                Map.of(
                        "statusDate", dateAndTime,
                        "statusTime", timeSpinner
                )
        );
        validationUI.bindResetOnChange();

        calendarView = new PlannerCalendarView();
        calendarContainer.getChildren().add(calendarView);

        updateHeader(calendarView.getMonth());

        calendarView.selectedDateProperty().addListener((obs, oldD, newD) ->
                loadForDate(newD));

        statusEditPolicy.configure(shipmentStatusComboBox);

        toLoadListView.setItems(toLoadItems);
        toDropListView.setItems(toDropItems);
        shipmentsListView.setItems(shipmentsItems);

        setupList(toLoadListView, toDropListView, TransportEventType.LOAD);
        setupList(toDropListView, toLoadListView, TransportEventType.DROP);
        setupSingleList(shipmentsListView);

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
    }

    private void setupList(ListView<ShipmentListItemDto> list,
                           ListView<ShipmentListItemDto> otherList,
                           TransportEventType eventType) {
        list.setCellFactory(lv -> {
            ShipmentCell cell = new ShipmentCell(windowService);
            cell.setActiveTab(ActiveTab.MY_TRANSPORTS);
            return cell;
        });

        list.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null) return;
                    state.setSelectedTransportEvent(eventType);

                    if (otherList.getSelectionModel().getSelectedItem() != null) {
                        Platform.runLater(() ->
                                otherList.getSelectionModel().clearSelection());
                    }

                    Platform.runLater(() -> selectShipment(newItem));
                });
    }

    private void setupSingleList(ListView<ShipmentListItemDto> list) {
        list.setCellFactory(lv -> {
            ShipmentCell cell = new ShipmentCell(windowService);
            cell.setActiveTab(ActiveTab.MY_SHIPMENTS);
            return cell;
        });

        list.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem == null) return;
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
    public void handleLeft() {
        calendarView.setMonth(calendarView.getMonth().minusMonths(1));
        updateHeader(calendarView.getMonth());
    }

    @FXML
    public void handleRight() {
        calendarView.setMonth(calendarView.getMonth().plusMonths(1));
        updateHeader(calendarView.getMonth());
    }

    @FXML
    public void submitShipmentUpdate() {
        if (!formState.isDirty() || state.getSelectedShipment() == null) return;

        ShipmentUpdateDto updateDto = buildUpdateDto();
        if (updateDto == null) return;

        try {
            ShipmentListItemDto fresh = shipmentClient.update(updateDto);
            applyShipmentUpdate(fresh);
        } catch (ApiException e) {
            log.warn("Shipment update rejected by backend", e);
            AlertUtils.show(ApiErrorUtils.resolve(e, "Shipment update failed."));
        }
    }

    /* ================= Update / Mutation ================= */
    private void applyShipmentUpdate(ShipmentListItemDto fresh) {
        if (fresh == null) return;

        replaceIn(shipmentsItems, fresh);
        replaceIn(toLoadItems, fresh);
        replaceIn(toDropItems, fresh);

        resortByStatus(shipmentsItems);
        resortByStatus(toLoadItems);
        resortByStatus(toDropItems);

        if (state.getSelectedShipment() != null
                && Objects.equals(state.getSelectedShipment().id(), fresh.id())) {
            state.setSelectedShipment(fresh);
            if (selectionService.isShipmentTab(state)) showMyShipmentDetails(fresh);
            else showMyTransportDetails(fresh);
        }
    }

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
    }

    @FXML
    public void handleCancel() {
        if (state.getSelectedShipment() == null) return;

        boolean confirmed = AlertUtils.showConfirmation(
                "Cancellation",
                "Are you sure you want to cancel shipment " + state.getSelectedShipment().id() + "?"
        );
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
    private void loadForDate(LocalDate date) {
        state.setSelectedDate(date);

        CompletableFuture
                .supplyAsync(() -> dataService.loadMyShipments(date))
                .thenCombine(CompletableFuture.supplyAsync(() -> dataService.loadMyTransports(date)), Map::entry)
                .thenAccept(result ->
                        Platform.runLater(() -> {
                            clearSelection();
                            applyDayData(result.getKey(), result.getValue());
                        })
                )
                .exceptionally(ex -> {
                    log.warn("Failed to reload planner for date {}", date, ex);
                    return null;
                });
    }

    private void invalidateShipment(Long shipmentId) {
        if (shipmentId == null) return;

        CompletableFuture
                .supplyAsync(() -> dataService.loadShipmentById(shipmentId))
                .thenAccept(fresh -> Platform.runLater(() -> {
                    if (fresh == null) return;

                    replaceIn(shipmentsItems, fresh);
                    replaceIn(toLoadItems, fresh);
                    replaceIn(toDropItems, fresh);

                    if (state.getSelectedShipment() != null
                            && Objects.equals(state.getSelectedShipment().id(), shipmentId)) {
                        state.setSelectedShipment(fresh);
                        if (selectionService.isShipmentTab(state)) showMyShipmentDetails(fresh);
                        else showMyTransportDetails(fresh);
                    }
                }))
                .exceptionally(ex -> {
                    log.warn("Failed to refresh shipment {}", shipmentId, ex);
                    return null;
                });
    }

    private void resortByStatus(ObservableList<ShipmentListItemDto> list) {
        if (list.size() < 2) return;
        FXCollections.sort(list, sortingService.byStatus());
    }

    private static void replaceIn(ObservableList<ShipmentListItemDto> list, ShipmentListItemDto fresh) {
        Long id = fresh.id();
        if (id == null) return;

        for (int i = 0; i < list.size(); i++) {
            ShipmentListItemDto it = list.get(i);
            if (it != null && Objects.equals(it.id(), id)) {
                list.set(i, fresh);
                return;
            }
        }
    }

    private void reloadAfterMutation() {
        loadMyShipments();
        loadMyTransports();

        ActiveTab preferredTab = state.getActiveTab();

        if (preferredTab == ActiveTab.MY_TRANSPORTS && state.isHasMyTransports()) {
            switchTab(ActiveTab.MY_TRANSPORTS);
        } else if (preferredTab == ActiveTab.MY_SHIPMENTS && state.isHasMyShipments()) {
            clearSelection();
            switchTab(ActiveTab.MY_SHIPMENTS);
        } else if (state.isHasMyShipments()) {
            switchTab(ActiveTab.MY_SHIPMENTS);
        } else if (state.isHasMyTransports()) {
            switchTab(ActiveTab.MY_TRANSPORTS);
        } else {
            showEmptyState();
        }
    }

    /* ================= Async Data Apply ================= */
    private void applyDayData(List<ShipmentListItemDto> shipments, PlannerDataService.TransportBuckets buckets) {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
        contentContainer.setVisible(true);
        contentContainer.setManaged(true);

        // shipments
        shipmentsItems.setAll(shipments);
        state.setHasMyShipments(!shipments.isEmpty());

        // transports
        toLoadItems.setAll(buckets.toLoad());
        toDropItems.setAll(buckets.toDrop());
        state.setHasMyTransports(!buckets.toLoad().isEmpty() || !buckets.toDrop().isEmpty());

        applyTabsVisibilityRules();
        updateTransportVisibility();

        if (state.isHasMyShipments()) {
            switchTab(ActiveTab.MY_SHIPMENTS);
        } else if (state.isHasMyTransports()) {
            switchTab(ActiveTab.MY_TRANSPORTS);
        } else {
            showEmptyState();
        }
    }

    /* ================= Selection & Tabs ================= */
    public void selectShipment(ShipmentListItemDto dto) {
        state.setSelectedShipment(dto);

        detailsStack.setVisible(true);
        detailsStack.setManaged(true);

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
                myShipmentsContainer.setVisible(true);
                myShipmentsContainer.setManaged(true);
                myTransportsContainer.setVisible(false);
                myTransportsContainer.setManaged(false);
            }
            case MY_TRANSPORTS -> {
                setActive(myTransports, myShipments);
                myTransportsContainer.setVisible(true);
                myTransportsContainer.setManaged(true);
                myShipmentsContainer.setVisible(false);
                myShipmentsContainer.setManaged(false);
            }
        }
    }

    /* ================= Data Loading ================= */
    private void loadMyTransports() {
        var buckets = dataService.loadMyTransports(state.getSelectedDate());

        toLoadItems.setAll(buckets.toLoad());
        toDropItems.setAll(buckets.toDrop());

        state.setHasMyTransports(!toLoadItems.isEmpty() || !toDropItems.isEmpty());

        applyTabsVisibilityRules();
        updateTransportVisibility();
    }

    private void loadMyShipments() {
        var items = dataService.loadMyShipments(state.getSelectedDate());
        shipmentsItems.setAll(items);

        state.setHasMyShipments(!shipmentsItems.isEmpty());

        applyTabsVisibilityRules();
        updateMyShipmentsVisibility();
    }

    /* ================= Details ================= */
    private void showMyShipmentDetails(ShipmentListItemDto dto) {
        detailsPresenter.showShipmentDetails(
                dto,
                shipmentNumber,
                dispatcher,
                shipmentDetailsContainer,
                transportDetailsContainer,
                timelineContainer,
                cancelButton
        );
    }

    private void showMyTransportDetails(ShipmentListItemDto dto) {
        initializingForm = true;
        formState.reset();
        submitButton.setDisable(true);

        ShipmentContext ctx = selectionService.buildContext(dto, state.getSelectedDate(), state.getSelectedTransportEvent());

        detailsPresenter.showTransportDetails(
                dto,
                ctx,
                shipmentNumberTransport,
                shipmentDetailsContainer,
                transportDetailsContainer,
                carrierField,
                commentsTextArea,
                licensePlateField,
                transportOrder,
                shipmentStatusComboBox,
                dateAndTime,
                timeSpinner
        );

        formState.bindTo(dto);
        initializingForm = false;
        updateSubmitState();
    }

    private void hideDetailsCompletely() {
        detailsStack.setVisible(false);
        detailsStack.setManaged(false);
        detailsPresenter.hideAll(shipmentDetailsContainer, transportDetailsContainer, timelineContainer, cancelButton);
    }

    private void clearSelection() {
        state.setSelectedShipment(null);

        toLoadListView.getSelectionModel().clearSelection();
        toDropListView.getSelectionModel().clearSelection();
        shipmentsListView.getSelectionModel().clearSelection();

        hideDetailsCompletely();

        carrierField.clear();
        commentsTextArea.clear();
        licensePlateField.clear();
        transportOrder.clear();
        shipmentStatusComboBox.setValue(null);

        statusEditPolicy.reset(dateAndTime, timeSpinner);

        formState.reset();
        updateSubmitState();
    }

    /* ================= UI Helpers ================= */
    private void updateHeader(YearMonth ym) {
        yearLabel.setText(String.valueOf(ym.getYear()));
        monthLabel.setText(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.UK)
        );
    }

    private void updateTransportVisibility() {
        boolean hasToLoad = !toLoadItems.isEmpty();
        boolean hasToDrop = !toDropItems.isEmpty();

        toLoadContainer.setVisible(hasToLoad);
        toLoadContainer.setManaged(hasToLoad);

        toDropContainer.setVisible(hasToDrop);
        toDropContainer.setManaged(hasToDrop);
    }

    private void updateMyShipmentsVisibility() {
        myShipmentsContainer.setVisible(true);
        myShipmentsContainer.setManaged(true);
    }

    private void applyTabsVisibilityRules() {
        boolean showEmpty = !state.isHasMyShipments() && !state.isHasMyTransports();

        emptyMessageLabel.setVisible(showEmpty);
        emptyMessageLabel.setManaged(showEmpty);

        buttonsContainer.setVisible(!showEmpty);
        buttonsContainer.setManaged(!showEmpty);

        myShipments.setVisible(state.isHasMyShipments());
        myShipments.setManaged(state.isHasMyShipments());

        myTransports.setVisible(state.isHasMyTransports());
        myTransports.setManaged(state.isHasMyTransports());
    }

    private void setActive(Button active, Button inactive) {
        if (!active.getStyleClass().contains("active-tab")) {
            active.getStyleClass().add("active-tab");
        }
        inactive.getStyleClass().remove("active-tab");
    }

    private void applyPermissions() {
        boolean canViewMyShipments = permissions.canViewMyShipments();
        myShipments.setDisable(!canViewMyShipments);
    }

    /* ================= Empty State ================= */
    private void showEmptyState() {
        clearSelection();

        myShipmentsContainer.setVisible(false);
        myShipmentsContainer.setManaged(false);

        myTransportsContainer.setVisible(false);
        myTransportsContainer.setManaged(false);

        emptyMessageLabel.setVisible(true);
        emptyMessageLabel.setManaged(true);

        buttonsContainer.setVisible(false);
        buttonsContainer.setManaged(false);
    }

}
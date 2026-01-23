package com.mayak.iet.ui.workspace.planner.controller;

import com.mayak.iet.shipment.event.ShipmentEvent;
import com.mayak.iet.infrastructure.error.ApiErrorUtils;
import com.mayak.iet.infrastructure.tost.ToastService;
import com.mayak.iet.integration.api.CompanyClient;
import com.mayak.iet.integration.api.ShipmentClient;
import com.mayak.iet.integration.exception.ApiException;
import com.mayak.iet.domain.planner.policy.ShipmentExecutionPolicy;
import com.mayak.iet.domain.planner.policy.ShipmentPlannerPolicy;
import com.mayak.iet.domain.planner.service.ShipmentSortingService;
import com.mayak.iet.domain.planner.model.ShipmentContext;
import com.mayak.iet.domain.planner.timeline.ShipmentTimelineService;
import com.mayak.iet.company.dto.CompanyDto;
import com.mayak.iet.shipment.dto.enums.ShipmentCancelReasonDto;
import com.mayak.iet.shipment.dto.enums.TransportEventType;
import com.mayak.iet.shipment.dto.view.MyTransportEventDto;
import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.shipment.dto.view.ShipmentUpdateDto;
import com.mayak.iet.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.integration.websocket.ShipmentStompClient;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.core.SecuredView;
import com.mayak.iet.ui.core.UserPermissions;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.workspace.planner.base.ShipmentCell;
import com.mayak.iet.ui.workspace.planner.enums.ActiveTab;
import com.mayak.iet.ui.workspace.planner.form.ShipmentTransportFormState;
import com.mayak.iet.ui.workspace.planner.policy.PlannerStatusEditPolicy;
import com.mayak.iet.ui.workspace.planner.presenter.TimelinePresenter;
import com.mayak.iet.ui.workspace.planner.view.PlannerCalendarView;
import com.mayak.iet.ui.workspace.request.item.RefuseReasonController;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.common.TextUtils;
import com.mayak.iet.infrastructure.fx.AutoCompleteUtils;
import com.mayak.iet.infrastructure.time.DatePickerUtils;
import com.mayak.iet.infrastructure.time.TimeSpinnerUtils;
import com.mayak.iet.infrastructure.ui.ValidationUIHelper;
import com.mayak.iet.infrastructure.window.WindowService;
import com.mayak.iet.support.validation.StatusDateTimeValidator;
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
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class PlannerController implements SecuredView, ViewLifecycle {

    @FXML public Label yearLabel, monthLabel, shipmentNumber, shipmentNumberTransport, dispatcher, emptyMessageLabel;
    @FXML public Button leftButton, rightButton;
    @FXML public VBox shipmentDetailsContainer, timelineContainer, transportDetailsContainer, myShipmentsContainer, myTransportsContainer,
            toLoadContainer, toDropContainer;
    @FXML public HBox buttonsContainer;
    @Getter
    @FXML public DatePicker dateAndTime;
    @Getter
    @FXML public Spinner<LocalTime> timeSpinner;
    @FXML private StackPane calendarContainer, detailsStack;
    @FXML public TextArea commentsTextArea;
    @FXML public TextField carrierField, licensePlateField, transportOrder;
    @FXML public ComboBox<ShipmentStatusDto> shipmentStatusComboBox;
    @FXML public Button myShipments, myTransports, cancelButton, submitButton;
    @FXML private ListView<ShipmentListItemDto> toLoadListView, toDropListView, shipmentsListView;

    private final ShipmentPlannerPolicy plannerPolicy;
    private final ShipmentExecutionPolicy detailsPolicy;
    private final PlannerStatusEditPolicy statusEditPolicy;
    private final ShipmentTimelineService timelineService;
    private final ShipmentSortingService shipmentSortingService;
    private final TimelinePresenter timelinePresenter;
    private final ShipmentTransportFormState formState = new ShipmentTransportFormState();
    private final ShipmentStompClient shipmentWsClient;

    private final ObservableList<ShipmentListItemDto> toLoadItems = FXCollections.observableArrayList();
    private final ObservableList<ShipmentListItemDto> toDropItems = FXCollections.observableArrayList();
    private final ObservableList<ShipmentListItemDto> shipmentsItems = FXCollections.observableArrayList();

    private final WindowService windowService;
    private final CompanyClient companyClient;
    private final ShipmentClient shipmentClient;
    private ValidationUIHelper validationUI;
    private UserPermissions permissions;

    private PlannerCalendarView calendarView;
    private LocalDate selectedDate = LocalDate.now();

    private ShipmentListItemDto selectedShipment;

    private ActiveTab activeTab;
    private boolean hasMyShipments = false;
    private boolean hasMyTransports = false;

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.permissions = new UserPermissions(user);
    }

    @Override
    public void onShow() {
        applyPermissions();
        initInitialView();
        initShipmentRealtimeUpdates();
    }

    private void initShipmentRealtimeUpdates() {
        shipmentWsClient.connect(
                event -> Platform.runLater(() -> handleShipmentEvent(event)),
                event -> Platform.runLater(() -> handleShipmentUserEvent(event))
        );
    }

    private void handleShipmentEvent(ShipmentEvent<?> event) {
        loadMyShipments();
        loadMyTransports();
    }

    private void handleShipmentUserEvent(ShipmentEvent<?> event) {
        if (event.getType() == ShipmentEvent.EventType.CANCELED) {
            ToastService.showInfo(
                    windowService.getPrimaryStage(),
                    "Shipment canceled",
                    "Shipment #" + event.getShipmentId() + " has been canceled"
            );
        }
    }

    @Override
    public void onHide() {
        shipmentWsClient.disconnect();
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

        calendarView.selectedDateProperty().addListener((obs, oldD, newD) -> {
            selectedDate = newD;
            loadMyShipments();
            loadMyTransports();

            if (hasMyShipments) {
                switchTab(ActiveTab.MY_SHIPMENTS);
            } else if (hasMyTransports) {
                switchTab(ActiveTab.MY_TRANSPORTS);
            } else {
                showEmptyState();
            }
        });

        statusEditPolicy.configure(shipmentStatusComboBox);

        toLoadListView.setItems(toLoadItems);
        toDropListView.setItems(toDropItems);
        shipmentsListView.setItems(shipmentsItems);

        setupList(toLoadListView, toDropListView);
        setupList(toDropListView, toLoadListView);
        setupSingleList(shipmentsListView);

        setupDirtyListeners();

        Set<String> companies = companyClient.findAll()
                .stream()
                .map(CompanyDto::name)
                .collect(Collectors.toSet());

        AutoCompleteUtils.setupAutoCompletion(carrierField, companies);
        DatePickerUtils.setupDatePicker(dateAndTime);
        TimeSpinnerUtils.setupTimeSpinner(timeSpinner, 15);
    }

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

    private void setupList(ListView<ShipmentListItemDto> list, ListView<ShipmentListItemDto> otherList) {
        list.setCellFactory(lv -> new ShipmentCell(windowService));

        list.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {

                    if (newItem == null) {
                        clearSelection();
                        return;
                    }

                    if (otherList.getSelectionModel().getSelectedItem() != null) {
                        otherList.getSelectionModel().clearSelection();
                    }

                    selectShipment(newItem);
                });
    }

    private void setupSingleList(ListView<ShipmentListItemDto> list) {
        list.setCellFactory(lv -> new ShipmentCell(windowService));

        list.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem == null) {
                        clearSelection();
                        return;
                    }
                    selectShipment(newItem);
                });
    }

    @FXML
    public void submitShipmentUpdate() {
        if (!formState.isDirty() || selectedShipment == null) return;

        ShipmentUpdateDto updateDto = buildUpdateDto();
        if (updateDto == null) return;

        ActiveTab tabBeforeUpdate = activeTab;

        try {
            shipmentClient.update(updateDto);
            reloadAfterMutation(tabBeforeUpdate);
        } catch (ApiException e) {
            log.warn("Shipment update rejected by backend", e);
            AlertUtils.show(ApiErrorUtils.resolve(e, "Shipment update failed."));
        }
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
                    if (selectedShipment == null) return;

                    statusEditPolicy.apply(newVal, dateAndTime, timeSpinner);
                    markDirtyIfChanged();
                });
    }

    private void markDirtyIfChanged() {
        if (selectedShipment == null) return;

        formState.updateCurrent(
                carrierField.getText(),
                commentsTextArea.getText(),
                licensePlateField.getText(),
                transportOrder.getText(),
                shipmentStatusComboBox.getValue()
        );

        updateSubmitState();
    }

    private void updateSubmitState() {
        submitButton.setDisable(!formState.isDirty());
    }

    private ShipmentUpdateDto buildUpdateDto() {
        if (selectedShipment == null) return null;

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

        return formState.toUpdateDto(selectedShipment.id(), statusAt);
    }

    private void loadMyTransports() {
        clearSelection();

        toLoadItems.clear();
        toDropItems.clear();

        LocalDate date = selectedDate != null ? selectedDate : LocalDate.now();

        List<MyTransportEventDto> events =
                shipmentClient.findMyTransportEvents(date);

        List<ShipmentListItemDto> toLoad =
                events.stream()
                        .filter(e -> e.eventType() == TransportEventType.LOAD)
                        .map(MyTransportEventDto::shipment)
                        .sorted(shipmentSortingService.byStatusThenTime(selectedDate))
                        .toList();

        List<ShipmentListItemDto> toDrop =
                events.stream()
                        .filter(e -> e.eventType() == TransportEventType.DROP)
                        .map(MyTransportEventDto::shipment)
                        .sorted(shipmentSortingService.byStatusThenTime(selectedDate))
                        .toList();

        toLoadItems.setAll(toLoad);
        toDropItems.setAll(toDrop);

        hasMyTransports = !toLoadItems.isEmpty() || !toDropItems.isEmpty();

        applyTabsVisibilityRules();
        updateTransportVisibility();
    }

    private void loadMyShipments() {
        clearSelection();
        shipmentsItems.clear();

        LocalDate date = selectedDate != null ? selectedDate : LocalDate.now();
        shipmentsItems.addAll(shipmentClient.findMyShipments(date).stream()
                .sorted(shipmentSortingService.byStatusThenTime(selectedDate))
                .toList());

        hasMyShipments = !shipmentsItems.isEmpty();
        applyTabsVisibilityRules();

        updateMyShipmentsVisibility();
    }

    public void selectShipment(ShipmentListItemDto dto) {
        this.selectedShipment = dto;

        detailsStack.setVisible(true);
        detailsStack.setManaged(true);

        if (activeTab == ActiveTab.MY_SHIPMENTS) {
            showMyShipmentDetails(dto);
        } else if (activeTab == ActiveTab.MY_TRANSPORTS) {
            showMyTransportDetails(dto);
        }
    }

    private void showMyShipmentDetails(ShipmentListItemDto dto) {
        transportDetailsContainer.setVisible(false);
        transportDetailsContainer.setManaged(false);

        shipmentDetailsContainer.setVisible(true);
        shipmentDetailsContainer.setManaged(true);

        shipmentNumber.setText(String.valueOf(dto.id()));
        dispatcher.setText(dto.dispatcher() != null ? dto.dispatcher().fullName() : "-");
        timelineContainer.getChildren().clear();

        boolean showTimeline = detailsPolicy.showTimeline(dto);
        boolean canCancel = detailsPolicy.canCancel(dto);

        timelineContainer.setVisible(showTimeline);
        timelineContainer.setManaged(showTimeline);

        cancelButton.setVisible(canCancel);
        cancelButton.setManaged(canCancel);


        if (showTimeline) {
            var entries = timelineService.build(dto.timestamps());
            timelinePresenter.render(timelineContainer, entries);
        }
    }

    private void showMyTransportDetails(ShipmentListItemDto dto) {
        shipmentDetailsContainer.setVisible(false);
        shipmentDetailsContainer.setManaged(false);

        transportDetailsContainer.setVisible(true);
        transportDetailsContainer.setManaged(true);
        shipmentNumberTransport.setText(String.valueOf(dto.id()));

        carrierField.setText(dto.carrierName());
        commentsTextArea.setText(dto.shipmentComments());
        licensePlateField.setText(dto.licensePlate());
        transportOrder.setText(dto.transportOrder());

        formState.bindTo(dto);
        updateSubmitState();

        shipmentStatusComboBox.setValue(null);
        statusEditPolicy.reset(dateAndTime, timeSpinner);

        ShipmentContext ctx = new ShipmentContext(
                dto.status(),
                selectedDate,
                dto.startDate().toLocalDate(),
                dto.endDate().toLocalDate()
        );

        boolean fieldsEditable = plannerPolicy.canEditTransportFields(ctx);

        // --- fields editability ---
        carrierField.setDisable(!fieldsEditable);
        licensePlateField.setDisable(!fieldsEditable);
        transportOrder.setDisable(!fieldsEditable);

        // comments are ALWAYS editable
        commentsTextArea.setDisable(false);

        // --- status editability ---
        ShipmentStatusDto allowedStatus = fieldsEditable ? plannerPolicy.allowedNextStatus(ctx).orElse(null) : null;
        statusEditPolicy.applyAllowedStatus(shipmentStatusComboBox, allowedStatus);
    }

    private void clearSelection() {
        selectedShipment = null;

        toLoadListView.getSelectionModel().clearSelection();
        toDropListView.getSelectionModel().clearSelection();
        shipmentsListView.getSelectionModel().clearSelection();

        detailsStack.setVisible(false);
        detailsStack.setManaged(false);

        shipmentDetailsContainer.setVisible(false);
        shipmentDetailsContainer.setManaged(false);

        transportDetailsContainer.setVisible(false);
        transportDetailsContainer.setManaged(false);

        timelineContainer.getChildren().clear();

        carrierField.clear();
        commentsTextArea.clear();
        licensePlateField.clear();
        transportOrder.clear();
        shipmentStatusComboBox.setValue(null);

        statusEditPolicy.reset(dateAndTime, timeSpinner);

        formState.reset();
        updateSubmitState();
    }

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

    @FXML
    public void onMyShipmentsHandle() {
        if (!permissions.canViewMyShipments()) return;
        switchTab(ActiveTab.MY_SHIPMENTS);
    }

    @FXML
    public void onMyTransportsHandle() {
        switchTab(ActiveTab.MY_TRANSPORTS);
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

    private void initInitialView() {
        loadMyShipments();
        loadMyTransports();

        if (hasMyShipments) {
            switchTab(ActiveTab.MY_SHIPMENTS);
        } else if (hasMyTransports) {
            switchTab(ActiveTab.MY_TRANSPORTS);
        } else {
            showEmptyState();
        }
    }

    @FXML
    public void handleCancel() {
        if (selectedShipment == null) return;

        boolean confirmed = AlertUtils.showConfirmation(
                "Cancellation",
                "Are you sure you want to cancel shipment " + selectedShipment.id() + "?"
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
                                    shipmentClient.cancel(selectedShipment.id(), reason);
                                    reloadAfterMutation(activeTab);
                                } catch (ApiException e) {
                                    log.warn("Failed to cancel shipment {}", selectedShipment.id(), e);
                                    AlertUtils.show(ApiErrorUtils.resolve(e, "Shipment cancellation failed."));
                                }
                            }
                    );
                },
                "REASON",
                null,
                null
        );
    }

    private void applyTabsVisibilityRules() {
        boolean showEmpty = !hasMyShipments && !hasMyTransports;

        emptyMessageLabel.setVisible(showEmpty);
        emptyMessageLabel.setManaged(showEmpty);

        buttonsContainer.setVisible(!showEmpty);
        buttonsContainer.setManaged(!showEmpty);

        myShipments.setVisible(hasMyShipments);
        myShipments.setManaged(hasMyShipments);

        myTransports.setVisible(hasMyTransports);
        myTransports.setManaged(hasMyTransports);
    }

    private void switchTab(ActiveTab tab) {
        this.activeTab = tab;

        clearSelection();

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

    private void reloadAfterMutation(ActiveTab preferredTab) {
        loadMyShipments();
        loadMyTransports();

        if (preferredTab == ActiveTab.MY_TRANSPORTS && hasMyTransports) {
            switchTab(ActiveTab.MY_TRANSPORTS);
            return;
        }

        if (preferredTab == ActiveTab.MY_SHIPMENTS && hasMyShipments) {
            switchTab(ActiveTab.MY_SHIPMENTS);
            return;
        }

        if (hasMyShipments) {
            switchTab(ActiveTab.MY_SHIPMENTS);
        } else if (hasMyTransports) {
            switchTab(ActiveTab.MY_TRANSPORTS);
        } else {
            showEmptyState();
        }
    }

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
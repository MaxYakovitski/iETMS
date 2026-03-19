package com.mayak.ietms.ui.workspace.request.filter;

import com.mayak.ietms.domain.request.filter.RequestFilterContext;
import com.mayak.ietms.domain.request.filter.RequestFilterPolicy;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import com.mayak.ietms.request.dto.filter.DangerousFilterOption;
import com.mayak.ietms.request.dto.filter.DatesFilterOption;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.integration.api.CompanyClient;
import com.mayak.ietms.integration.api.LocationClient;
import com.mayak.ietms.integration.api.UserClient;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.UserPermissions;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.ui.workspace.request.base.RequestsParent;
import com.mayak.ietms.infrastructure.fx.AutoCompleteUtils;
import com.mayak.ietms.infrastructure.time.DatePickerUtils;
import com.mayak.ietms.infrastructure.fx.MultiSelectComboBoxUtils;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.assembler.RequestFilterFormAssembler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.controlsfx.control.CheckComboBox;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
public class RequestFilterController implements SecuredView, ViewLifecycle {

    @FXML public CheckComboBox<RequestStatusDto> statusComboBox;
    @FXML public Label requestLabel;
    @FXML private ToggleGroup requestTypeGroup, shipmentTypeGroup;
    @FXML public RadioButton spotRadioButton, contractRadioButton, ftlRadioButton, ltlRadioButton;

    @FXML public TextField fromIsoField, fromZipField, fromPlaceField, toIsoField, toZipField, toPlaceField, companyField,
            weightFrom, weightTo, ldmFromField, ldmToField;

    @FXML public DatePicker startDatePicker, endDatePicker;

    @FXML public CheckComboBox <TransportTypeDto> transportComboBox;
    @FXML public ComboBox<DangerousFilterOption> adrComboBox;
    @FXML public ComboBox <DatesFilterOption> typeOfDateComboBox;
    @FXML public HBox requestTypeRow;
    @FXML private GridPane grid;

    @FXML public CheckComboBox <UserResponseDto> authorsMultiSelectComboBox;
    @FXML public CheckComboBox <UserResponseDto> competitorsMultiSelectComboBox;
    @FXML public CheckComboBox <UserResponseDto> dispatcherMultiSelectComboBox;

    @Setter
    private Stage stage;
    @Getter
    private UserResponseDto loggedInUser;
    private UserPermissions permissions;

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
    }

    @Setter
    private RequestsParent requestsParent;

    private final RequestFilterFormAssembler requestFilterFormAssembler;

    private final LocationClient locationClient;
    private final CompanyClient companyClient;
    private final UserClient userClient;

    private static final String REQUESTS_BY_COLLEAGUES = "requests by";
    private static final String JOINED_TO_REQUESTS = "joined";
    private static final String DISPATCHED_TO = "dispatched to";

    @Override
    public void onShow() {

        if (!permissions.canViewClientRequests()) {
            requestLabel.setVisible(false);
            requestLabel.setManaged(false);

            requestTypeRow.setVisible(false);
            requestTypeRow.setManaged(false);
            collapseRow(1);
        }

        setupLocationAutoComplete();
        setupCompanyAutoComplete();

        setupAuthorsAndCompetitors().thenRun(() ->
                Platform.runLater(() ->
                        requestsParent.getLastAppliedFilter().ifPresent(this::restorePreviousFilter)
                )
        );
    }


    @FXML
    public void initialize() {
        requestTypeGroup = new ToggleGroup();
        spotRadioButton.setToggleGroup(requestTypeGroup);
        contractRadioButton.setToggleGroup(requestTypeGroup);

        shipmentTypeGroup = new ToggleGroup();
        ftlRadioButton.setToggleGroup(shipmentTypeGroup);
        ltlRadioButton.setToggleGroup(shipmentTypeGroup);

        TextUtils.allowOnlyLatin(true, fromIsoField, fromZipField, fromPlaceField,
                toIsoField, toZipField, companyField,weightFrom, weightTo,  ldmFromField, ldmToField);

        spotRadioButton.setUserData(RequestTypeDto.SPOT);
        contractRadioButton.setUserData(RequestTypeDto.CONTRACT);

        startDatePicker.setDisable(true);
        endDatePicker.setDisable(true);

        typeOfDateComboBox.setItems(FXCollections.observableArrayList(DatesFilterOption.values()));
        MultiSelectComboBoxUtils.setupSingleSelect(
                typeOfDateComboBox,
                List.of(DatesFilterOption.values()),
                Object::toString);

        RequestFilterPolicy policy = new RequestFilterPolicy();

        typeOfDateComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    boolean enabled = policy.areDatesEnabled(newVal);
                    startDatePicker.setDisable(!enabled);
                    endDatePicker.setDisable(!enabled);
                });

        ftlRadioButton.setUserData(ShipmentTypeDto.FTL);
        ltlRadioButton.setUserData(ShipmentTypeDto.LTL);


        MultiSelectComboBoxUtils.setupMultiSelect(
                statusComboBox,
                Arrays.asList(RequestStatusDto.values()),
                "status",
                Object::toString
        );

        MultiSelectComboBoxUtils.setupMultiSelect(
                transportComboBox,
                Arrays.asList(TransportTypeDto.values()),
                "transport",
                Object::toString
        );

        adrComboBox.setItems(FXCollections.observableArrayList(DangerousFilterOption.values()));
        MultiSelectComboBoxUtils.setupSingleSelect(
                adrComboBox,
                List.of(DangerousFilterOption.values()),
                Object::toString);

        DatePickerUtils.setupDatePickers(startDatePicker, endDatePicker);

        makeRadioButtonsDeselected(requestTypeGroup);
        makeRadioButtonsDeselected(shipmentTypeGroup);
    }

    @FXML
    public void handleSubmit() {
        RequestFilterDto filter = requestFilterFormAssembler.build(
                statusComboBox,
                requestTypeGroup,
                fromIsoField,
                fromZipField,
                fromPlaceField,
                toIsoField,
                toZipField,
                toPlaceField,
                companyField,
                typeOfDateComboBox,
                startDatePicker,
                endDatePicker,
                shipmentTypeGroup,
                transportComboBox,
                adrComboBox,
                weightFrom,
                weightTo,
                ldmFromField,
                ldmToField,
                authorsMultiSelectComboBox,
                competitorsMultiSelectComboBox,
                dispatcherMultiSelectComboBox
        );

        RequestFilterContext context = new RequestFilterContext(filter);
        boolean active = context.isActive();

        if (requestsParent != null) {
            HomeController home = requestsParent.getHomeController();
            if (home != null) {
                home.updateFilterState(active);
            }
        }

        if (requestsParent != null) {
            Platform.runLater(() -> requestsParent.applyFilter(filter));
        }
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    public void handleReset() {
        if (requestsParent != null) {
            requestsParent.safeUpdate(() -> {
                statusComboBox.getCheckModel().clearChecks();
                transportComboBox.getCheckModel().clearChecks();
                authorsMultiSelectComboBox.getCheckModel().clearChecks();
                competitorsMultiSelectComboBox.getCheckModel().clearChecks();
                dispatcherMultiSelectComboBox.getCheckModel().clearChecks();

                requestTypeGroup.selectToggle(null);
                shipmentTypeGroup.selectToggle(null);

                clearFields(fromIsoField, fromZipField, fromPlaceField, toIsoField, toZipField, toPlaceField, companyField,
                        weightFrom, weightTo, ldmFromField, ldmToField);

                typeOfDateComboBox.getSelectionModel().clearSelection();

                startDatePicker.setValue(null);
                endDatePicker.setValue(null);

                adrComboBox.getSelectionModel().clearSelection();

                HomeController home = requestsParent.getHomeController();
                if (home != null) {
                    home.updateFilterState(false);
                }
                requestsParent.clearFilter();
            });
        }
    }

    @FXML
    public void handleCancel() {
        if (stage != null) stage.close();
    }

    private void setupCompanyAutoComplete() {
        CompletableFuture.supplyAsync(() ->
                        companyClient.findAll().stream()
                                .map(CompanyDto::name)
                                .collect(Collectors.toSet()))
                .thenAccept(result -> Platform.runLater(() ->
                        AutoCompleteUtils.setupAutoCompletion(companyField, result)));
    }

    private void setupLocationAutoComplete() {
        CompletableFuture.supplyAsync(locationClient::findAll)
                .thenAccept(locs -> Platform.runLater(() -> {
                    Set<String> iso = locs.stream().map(LocationDto::countryCode).collect(Collectors.toSet());
                    Set<String> zip = locs.stream().map(LocationDto::zipCode).collect(Collectors.toSet());
                    Set<String> place = locs.stream().map(LocationDto::placeName).collect(Collectors.toSet());

                    AutoCompleteUtils.setupAutoCompletion(fromIsoField, iso);
                    AutoCompleteUtils.setupAutoCompletion(fromZipField, zip);
                    AutoCompleteUtils.setupAutoCompletion(fromPlaceField, place);
                    AutoCompleteUtils.setupAutoCompletion(toIsoField, iso);
                    AutoCompleteUtils.setupAutoCompletion(toZipField, zip);
                    AutoCompleteUtils.setupAutoCompletion(toPlaceField, place);
                }));
    }

    private CompletableFuture<Void> setupAuthorsAndCompetitors() {
        return CompletableFuture.supplyAsync(() -> {
            List<UserResponseDto> authors = permissions.isAdmin()
                    ? userClient.findClientSpecialists()
                    : userClient.findClientSpecialistsByDepartment(getUserDepartmentId());

            List<UserResponseDto> competitors = permissions.isAdmin()
                    ? userClient.findColleagues()
                    : userClient.findColleaguesByDepartment(getUserDepartmentId());

            return Map.entry(authors, competitors);
        }).thenAccept(result -> Platform.runLater(() -> {
            MultiSelectComboBoxUtils.setupMultiSelect(
                    authorsMultiSelectComboBox,
                    result.getKey(), REQUESTS_BY_COLLEAGUES,
                    u -> u.name() + " " + u.surname());
            MultiSelectComboBoxUtils.setupMultiSelect(
                    competitorsMultiSelectComboBox,
                    result.getValue(), JOINED_TO_REQUESTS,
                    u -> u.name() + " " + u.surname());
            MultiSelectComboBoxUtils.setupMultiSelect(
                    dispatcherMultiSelectComboBox,
                    result.getValue(), DISPATCHED_TO,
                    u -> u.name() + " " + u.surname());
        }));
    }

    private Long getUserDepartmentId() {
        return loggedInUser != null && loggedInUser.profile() != null
                ? loggedInUser.profile().departmentId()
                : null;
    }

    private void restorePreviousFilter(RequestFilterDto filter) {
        if (requestsParent == null) return;
        if (filter == null) return;

        Platform.runLater(() -> {
            statusComboBox.getCheckModel().clearChecks();
            filter.getStatuses().forEach(statusComboBox.getCheckModel()::check);

            if (filter.getRequestTypes() != null) {
                if (filter.getRequestTypes().contains(RequestTypeDto.SPOT.name())) spotRadioButton.setSelected(true);
                if (filter.getRequestTypes().contains(RequestTypeDto.CONTRACT.name())) contractRadioButton.setSelected(true);
            }

            restoreFields(filter);
            if (filter.getCompanyName() != null) {
                companyField.setText(filter.getCompanyName());
            } else {
                companyField.clear();
            }

            typeOfDateComboBox.setValue(filter.getDatesFilterOption());
            boolean datesEnabled = filter.getDatesFilterOption() != null;
            startDatePicker.setDisable(!datesEnabled);
            endDatePicker.setDisable(!datesEnabled);
            startDatePicker.setValue(filter.getStartDate());
            endDatePicker.setValue(filter.getEndDate());

            if (filter.getShipmentTypes() != null) {
                if (filter.getShipmentTypes().contains(ShipmentTypeDto.FTL)) ftlRadioButton.setSelected(true);
                if (filter.getShipmentTypes().contains(ShipmentTypeDto.LTL)) ltlRadioButton.setSelected(true);
            }

            transportComboBox.getCheckModel().clearChecks();
            if (filter.getTransportTypes() != null) {
                filter.getTransportTypes().forEach(transportComboBox.getCheckModel()::check);
            }

            if (filter.getDangerous() == null) {
                adrComboBox.getSelectionModel().clearSelection();
            } else {
                adrComboBox.getSelectionModel().select(filter.getDangerous());
            }

            weightFrom.setText(filter.getMinWeight() != null ? filter.getMinWeight().toString() : "");
            weightTo.setText(filter.getMaxWeight() != null ? filter.getMaxWeight().toString() : "");

            ldmFromField.setText(filter.getMinLdm() != null ? filter.getMinLdm().toString() : "");
            ldmToField.setText(filter.getMaxLdm() != null ? filter.getMaxLdm().toString() : "");

            authorsMultiSelectComboBox.getCheckModel().clearChecks();
            if (filter.getAuthorIds() != null) {
                authorsMultiSelectComboBox.getItems().stream()
                        .filter(u -> filter.getAuthorIds().contains(u.id()))
                        .forEach(authorsMultiSelectComboBox.getCheckModel()::check);
            }

            competitorsMultiSelectComboBox.getCheckModel().clearChecks();
            if (filter.getCompetitorIds() != null) {
                competitorsMultiSelectComboBox.getItems().stream()
                        .filter(u -> filter.getCompetitorIds().contains(u.id()))
                        .forEach(competitorsMultiSelectComboBox.getCheckModel()::check);
            }

            dispatcherMultiSelectComboBox.getCheckModel().clearChecks();
            if(filter.getDispatchersIds() != null) {
                dispatcherMultiSelectComboBox.getItems().stream()
                        .filter(u -> filter.getDispatchersIds().contains(u.id()))
                        .forEach(dispatcherMultiSelectComboBox.getCheckModel()::check);
            }
        });
    }

    private void makeRadioButtonsDeselected(ToggleGroup group) {
        group.getToggles().forEach(toggle -> {
            RadioButton rb = (RadioButton) toggle;
            rb.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                if (rb.equals(group.getSelectedToggle())) {
                    group.selectToggle(null);
                    e.consume();
                }
            });
        });
    }


    private void clearFields(TextField ... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private void restoreFields(RequestFilterDto filter) {
        Map<TextField, Supplier<String>> fieldMapping = Map.of(
                fromIsoField, filter::getFromCountry,
                fromZipField, filter::getFromZipCode,
                fromPlaceField, filter::getFromPlace,
                toIsoField, filter::getToCountry,
                toZipField, filter::getToZipCode,
                toPlaceField, filter::getToPlace
        );

        fieldMapping.forEach((field, valueSupplier) -> {
            String value = valueSupplier.get();
            if (value != null) {
                field.setText(value);
            } else {
                field.clear();
            }
        });
    }

    private void collapseRow(int removedRowIndex) {
        for (Node node : grid.getChildren()) {
            Integer row = GridPane.getRowIndex(node);
            if (row == null) continue;
            if (row > removedRowIndex) {
                GridPane.setRowIndex(node, row - 1);
            }
        }
    }
}
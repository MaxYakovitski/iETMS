package com.mayak.iet.infrastructure.assembler;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.enums.ShipmentTypeDto;
import com.mayak.iet.request.dto.enums.TransportTypeDto;
import com.mayak.iet.request.dto.filter.DangerousFilterOption;
import com.mayak.iet.request.dto.filter.DatesFilterOption;
import com.mayak.iet.request.dto.filter.RequestFilterDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.infrastructure.common.TextUtils;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.CheckComboBox;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestFilterFormAssembler {

    public RequestFilterDto build(
            CheckComboBox<RequestStatusDto> statusBox,
            ToggleGroup requestTypeGroup,
            TextField fromIsoField,
            TextField fromZipField,
            TextField fromPlaceFiled,
            TextField toIsoField,
            TextField toZipField,
            TextField toPlaceFiled,
            TextField companyField,
            ComboBox<DatesFilterOption> datesFilterOptions,
            DatePicker startDate,
            DatePicker endDate,
            ToggleGroup shipmentTypeGroup,
            CheckComboBox<TransportTypeDto> transportBox,
            ComboBox <DangerousFilterOption>dangerousComboBox,
            TextField weightFrom,
            TextField weightTo,
            TextField ldmFrom,
            TextField ldmTo,
            CheckComboBox<UserResponseDto> authorsBox,
            CheckComboBox<UserResponseDto> competitorsBox,
            CheckComboBox<UserResponseDto> dispatchersBox
    ) {
        RequestFilterDto filter = new RequestFilterDto();

        // ==================== REQUEST STATUSES ====================
        filter.setStatuses(safeChecked(statusBox));

        // ==================== REQUEST TYPE ====================
        filter.setRequestTypes(safeToggleToString(requestTypeGroup));

        // ==================== LOCATIONS ====================
        filter.setFromCountry(TextUtils.safeTrim(fromIsoField.getText()));
        filter.setFromZipCode(TextUtils.safeTrim(fromZipField.getText()));
        filter.setFromPlace(TextUtils.safeTrim(fromPlaceFiled.getText()));

        filter.setToCountry(TextUtils.safeTrim(toIsoField.getText()));
        filter.setToZipCode(TextUtils.safeTrim(toZipField.getText()));
        filter.setToPlace(TextUtils.safeTrim(toPlaceFiled.getText()));

        // ==================== COMPANY ====================
        filter.setCompanyName(TextUtils.safeTrim(companyField.getText()));

        // ==================== DATES ====================
        filter.setDatesFilterOption(datesFilterOptions.getValue());
        filter.setStartDate(startDate.getValue());
        filter.setEndDate(endDate.getValue());

        // ==================== SHIPMENT TYPE ====================
        filter.setShipmentTypes(safeToggleToEnum(shipmentTypeGroup, ShipmentTypeDto.class));

        // ==================== TRANSPORT ====================
        filter.setTransportTypes(safeChecked(transportBox));

        // ==================== DANGEROUS ====================
        filter.setDangerous(dangerousComboBox.getValue());

        // ==================== WEIGHT / LDM ====================
        filter.setMinWeight(TextUtils.parseDoubleSafe(weightFrom.getText()));
        filter.setMaxWeight(TextUtils.parseDoubleSafe(weightTo.getText()));
        filter.setMinLdm(TextUtils.parseDoubleSafe(ldmFrom.getText()));
        filter.setMaxLdm(TextUtils.parseDoubleSafe(ldmTo.getText()));

        // ==================== COLLEAGUES ====================
        filter.setAuthorIds(extractIds(authorsBox));
        filter.setCompetitorIds(extractIds(competitorsBox));
        filter.setDispatchersIds(extractIds(dispatchersBox));

        return filter;
    }

    // ==================== HELPERS ====================

    private static <T> List<T> safeChecked(CheckComboBox<T> box) {
        if (box == null || box.getCheckModel() == null) return Collections.emptyList();
        return List.copyOf(box.getCheckModel().getCheckedItems());
    }

    private static List<String> safeToggleToString(ToggleGroup group) {
        if (group == null || group.getSelectedToggle() == null) {
            return List.of();
        }

        Object data = group.getSelectedToggle().getUserData();

        if (data instanceof Enum<?> e) {
            return List.of(e.name());
        }

        if (data instanceof String s) {
            return List.of(s);
        }

        return List.of();
    }

    private static <E extends Enum<E>> List<E> safeToggleToEnum(ToggleGroup group, Class<E> enumClass) {
        if (group == null || group.getSelectedToggle() == null) {
            return List.of();
        }

        Object data = group.getSelectedToggle().getUserData();

        if (enumClass.isInstance(data)) {
            return List.of(enumClass.cast(data));
        }

        if (data instanceof String s) {
            try {
                return List.of(Enum.valueOf(enumClass, s));
            } catch (IllegalArgumentException ignored) {}
        }

        return List.of();
    }

    private static List<Long> extractIds(CheckComboBox<UserResponseDto> box) {
        if (box == null || box.getCheckModel() == null) return Collections.emptyList();
        return box.getCheckModel().getCheckedItems()
                .stream()
                .map(UserResponseDto::id)
                .toList();
    }
}
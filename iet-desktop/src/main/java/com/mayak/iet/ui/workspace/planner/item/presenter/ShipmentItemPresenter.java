package com.mayak.iet.ui.workspace.planner.item.presenter;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.infrastructure.common.TextUtils;
import org.springframework.stereotype.Component;

@Component
public class ShipmentItemPresenter {

    public ShipmentItemViewData present(ShipmentListItemDto dto) {
        return ShipmentItemViewData.builder()
                .rId(dto.id() != null ? String.valueOf(dto.id()) : "")
                .tId(TextUtils.safeTrim(dto.tId()))
                .customerReference(TextUtils.safeTrim(dto.customerReference()))
                .customer(TextUtils.safeTrim(dto.customerName()))
                .dataStart(dto.startDate() != null ? dto.startDate().format(TextUtils.DATE_FORMATTER) : "")
                .dataEnd(dto.endDate() != null ? dto.endDate().format(TextUtils.DATE_FORMATTER) : "")
                .shipmentType(dto.shipmentType() != null ? dto.shipmentType().name() : "")
                .transportType(dto.transportType() != null ? dto.transportType().name() : "")
                .requestType(dto.requestType() != null ? dto.requestType().name() : "")
                .dangerous(dto.dangerous() ? "ADR" : "")
                .temperature(TextUtils.formatTemperature(dto.temperature()))
                .weight(TextUtils.formatWeight(dto.weight()))
                .loadingMeters(TextUtils.formatLoadingMeters(dto.loadingMeter()))
                .customerPrice(TextUtils.formatMoney(dto.clientPrice()))
                .carrierPrice(TextUtils.formatMoney(dto.carrierPrice()))
                .authorName(TextUtils.safeTrim(dto.author().firstName()))
                .authorSurname(TextUtils.safeTrim(dto.author().lastName()))
                .carrier(TextUtils.safeTrim(dto.carrierName()))
                .hasComments(dto.requestComments() != null && !dto.requestComments().isBlank())
                .build();
    }
}
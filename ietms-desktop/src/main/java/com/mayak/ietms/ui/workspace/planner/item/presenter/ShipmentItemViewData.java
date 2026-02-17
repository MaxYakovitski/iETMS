package com.mayak.ietms.ui.workspace.planner.item.presenter;

import lombok.Builder;

@Builder
public record ShipmentItemViewData (
        String rId,
        String tId,
        String customerReference,
        String customer,
        String dataStart,
        String dataEnd,
        String shipmentType,
        String transportType,
        String requestType,
        String dangerous,
        String temperature,
        String weight,
        String loadingMeters,
        String customerPrice,
        String carrierPrice,
        String authorName,
        String authorSurname,
        String carrier,
        boolean hasComments) {
}
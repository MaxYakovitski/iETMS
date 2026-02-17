package com.mayak.ietms.extension.dto;

import com.mayak.ietms.common.util.TextSanitizer;

public record ExtensionDraftIntent(
        String from,
        String to,
        String customerReference,
        String customerName,
        String startDate,
        String endDate,
        String shipmentType,
        String transportType,
        String temperature,
        Boolean dangerous,
        String weight,
        String loadingMeter,
        String comments) {

    public static ExtensionDraftIntent from(ExtensionRequestDraftDto dto) {
        if (dto == null) return null;

        return new ExtensionDraftIntent(
                TextSanitizer.safeTrim(dto.from()),
                TextSanitizer.safeTrim(dto.to()),
                TextSanitizer.safeTrim(dto.customerReference()),
                TextSanitizer.safeTrim(dto.customerName()),
                TextSanitizer.safeTrim(dto.startDate()),
                TextSanitizer.safeTrim(dto.endDate()),
                TextSanitizer.safeTrim(dto.shipmentType()),
                TextSanitizer.safeTrim(dto.transportType()),
                TextSanitizer.safeTrim(dto.temperature()),
                dto.dangerous(),
                TextSanitizer.safeTrim(dto.weight()),
                TextSanitizer.safeTrim(dto.loadingMeter()),
                TextSanitizer.safeTrim(dto.comments())
        );
    }
}
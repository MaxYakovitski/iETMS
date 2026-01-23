package com.mayak.iet.extension.dto;

public record ExtensionRequestDraftDto(
        // ---- context ----
        String pageUrl,
        String pageTitle,

        // ---- user extracted data ----
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

        String comments,

        // ---- meta ----
        String extensionVersion,
        String capturedAt) {
}

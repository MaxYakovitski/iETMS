package com.mayak.iet.features.request.application.format;

import com.mayak.iet.location.dto.LocationDto;
import com.mayak.iet.features.request.domain.model.Request;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public final class ExchangeFormatter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static String format(List<LocationDto> from, List<LocationDto> to, Request request) {
        if (request == null) return "";

        StringBuilder sb = new StringBuilder();

        appendRoute(sb, from, to);
        appendDates(sb, request);
        appendTransport(sb, request);
        appendCargo(sb, request);
        appendComments(sb, request);

        return sb.toString().trim();
    }

    private static void appendRoute(StringBuilder sb, List<LocationDto> from, List<LocationDto> to) {
        String fromStr = joinLocations(from);
        String toStr   = joinLocations(to);

        if (!fromStr.isBlank() || !toStr.isBlank()) {
            sb.append(fromStr).append(" -> ").append(toStr).append("\n");
        }
    }

    private static void appendDates(StringBuilder sb, Request request) {
        if (request.getStartDate() != null && request.getEndDate() != null) {
            sb.append(request.getStartDate().format(DATE))
                    .append(" -> ")
                    .append(request.getEndDate().format(DATE))
                    .append("\n");
        }
    }

    private static void appendTransport(StringBuilder sb, Request request) {
        if (request.getTransportType() != null || request.getShipmentType() != null) {
            if (request.getShipmentType() != null) {
                sb.append(request.getShipmentType());
            }
            if (request.getTransportType() != null) {
                sb.append(", ").append(request.getTransportType());
            }
            sb.append("\n");
        }
    }

    private static void appendCargo(StringBuilder sb, Request request) {
        boolean hasData =
                request.isDangerous()
                        || request.getTemperature() != null
                        || request.getWeight() != null
                        || request.getLoadingMeter() != null;

        if (!hasData) {
            return;
        }

        if (request.isDangerous()) {
            sb.append("ADR, ");
        }

        if (request.getTemperature() != null) {
            sb.append(request.getTemperature()).append(" °C, ");
        }

        if (request.getWeight() != null) {
            sb.append(request.getWeight()).append(" kg, ");
        }

        if (request.getLoadingMeter() != null) {
            sb.append(request.getLoadingMeter()).append(" ldm");
        }

        sb.append("\n");
    }

    private static void appendComments(StringBuilder sb, Request request) {
        if (request.getComments() != null && !request.getComments().isBlank()) {
            sb.append(request.getComments());
        }
    }

    // --- helpers ---
    private static String joinLocations(List<LocationDto> locations) {
        if (locations == null || locations.isEmpty()) return "";

        return locations.stream()
                .map(loc ->
                        loc.placeName() == null
                                ? loc.countryCode() + ", " + loc.zipCode()
                                : loc.countryCode() + ", " + loc.zipCode() + " " + loc.placeName())
                .collect(Collectors.joining(" + "));
    }
}
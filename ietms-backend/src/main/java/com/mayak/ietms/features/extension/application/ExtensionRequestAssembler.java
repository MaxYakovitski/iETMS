package com.mayak.ietms.features.extension.application;

import com.mayak.ietms.common.util.TextSanitizer;
import com.mayak.ietms.common.util.formatting.LocationTextParser;
import com.mayak.ietms.extension.dto.ExtensionRequestDraftDto;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.create.SpotRequestDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.enums.ShipmentTypeDto;
import com.mayak.ietms.request.dto.enums.TransportTypeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ExtensionRequestAssembler {

    private static final Pattern DATE_TOKEN = Pattern.compile(
            "\\b(\\d{2}[./-]\\d{2}[./-]\\d{4}|\\d{4}-\\d{2}-\\d{2})\\b");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    public BaseRequestDto build(ExtensionRequestDraftDto dto) {

        SpotRequestDto req = new SpotRequestDto();
        req.setType(RequestTypeDto.SPOT);

        req.setFromLocations(LocationTextParser.parseLocations(dto.from()));
        req.setToLocations(LocationTextParser.parseLocations(dto.to()));

        req.setCustomerName(TextSanitizer.safeTrim(dto.customerName()));
        req.setCustomerReference(TextSanitizer.safeTrim(dto.customerReference()));

        req.setStartDate(
                Optional.ofNullable(parseDateLoose(dto.startDate(), false))
                        .map(LocalDate::atStartOfDay)
                        .orElse(null)
        );

        req.setEndDate(
                Optional.ofNullable(parseDateLoose(dto.endDate(), true))
                        .map(LocalDate::atStartOfDay)
                        .orElse(null)
        );

        req.setShipmentType(parseShipmentType(dto.shipmentType()));
        req.setTransportType(parseTransportType(dto.transportType()));

        req.setTemperature(TextSanitizer.safeTrim(dto.temperature()));
        req.setDangerous(Boolean.TRUE.equals(dto.dangerous()));
        req.setWeight(parseDouble(dto.weight()));
        req.setLoadingMeter(parseDouble(dto.loadingMeter()));

        req.setComments(TextSanitizer.safeTrim(dto.comments()));

        return req;
    }

    private Double parseDouble(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String normalized = raw
                .replace(",", ".")
                .replaceAll("[^0-9.]", "");

        if (normalized.isBlank()) return null;

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ShipmentTypeDto parseShipmentType(String raw) {
        if (raw == null || raw.isBlank()) return null;

        try {
            return ShipmentTypeDto.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private TransportTypeDto parseTransportType(String raw) {
        return TransportTypeDto.fromLabel(raw);
    }

    private LocalDate parseDateLoose(String raw, boolean takeLast) {
        if (raw == null || raw.isBlank()) return null;

        Matcher matcher = DATE_TOKEN.matcher(raw);

        List<LocalDate> dates = new ArrayList<>();

        while (matcher.find()) {
            String token = matcher.group();
            LocalDate d = tryParseDate(token);
            if (d != null) {
                dates.add(d);
            }
        }

        if (dates.isEmpty()) return null;
        return takeLast ? dates.getLast() : dates.getFirst();
    }

    private LocalDate tryParseDate(String token) {
        for (DateTimeFormatter f : DATE_FORMATS) {
            try {
                return LocalDate.parse(token, f);
            } catch (Exception ignored) {}
        }
        return null;
    }
}

package com.mayak.iet.features.report.infra.excel;

import com.mayak.iet.user.dto.UserNameDto;
import com.mayak.iet.features.company.domain.model.Company;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.domain.model.SpotRequest;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RequestExcelRowMapper {

    public void writeRow(Sheet sheet, int rowIndex, Request request, CellStyle dateStyle, Map<Long, Location> locationById, Map<Long, UserNameDto> userNameById) {
        Row row = sheet.createRow(rowIndex);
        List<Location> fromLocations = orderedLocations(request.getFromLocationIds(), locationById);
        List<Location> toLocations = orderedLocations(request.getToLocationIds(), locationById);

        int cell = 0;

        row.createCell(cell++).setCellValue(request.getId());
        row.createCell(cell++).setCellValue(request instanceof SpotRequest ? "SPOT" : "CONTRACT");

        row.createCell(cell++).setCellValue(formatIsoList(fromLocations));
        row.createCell(cell++).setCellValue(formatIsoList(toLocations));

        row.createCell(cell++).setCellValue(formatLocations(fromLocations));
        row.createCell(cell++).setCellValue(formatLocations(toLocations));

        Company company = request.getCustomer();
        row.createCell(cell++).setCellValue(company != null ? nzs(company.getName()) : "");

        setDate(row, cell++, request.getStartDate(), dateStyle);
        setDate(row, cell++, request.getEndDate(), dateStyle);

        row.createCell(cell++).setCellValue(request.getShipmentType() != null ? request.getShipmentType().name() : "");
        row.createCell(cell++).setCellValue(request.getTransportType() != null ? request.getTransportType().name() : "");

        row.createCell(cell++).setCellValue(request.isDangerous() ? "yes" : "no");
        row.createCell(cell++).setCellValue(request.getTemperature() != null ? request.getTemperature() : "");
        row.createCell(cell++).setCellValue(request.getWeight() != null ? request.getWeight() : 0);
        row.createCell(cell++).setCellValue(request.getLoadingMeter() != null ? request.getLoadingMeter() : 0);
        row.createCell(cell++).setCellValue(request.getStatus().toString());
        row.createCell(cell++).setCellValue(getReason(request));
        row.createCell(cell++).setCellValue(request.getClientPrice() != null ? request.getClientPrice().doubleValue() : 0);
        row.createCell(cell++).setCellValue(request.getBidPrice() != null ? request.getBidPrice().doubleValue() : 0);
        row.createCell(cell++).setCellValue(request.getProfitMargin() != null ? request.getProfitMargin().doubleValue() : 0);

        row.createCell(cell++).setCellValue(nzs(userNameById.get(request.getAssignedUserId())));
        row.createCell(cell++).setCellValue(nzs(userNameById.get(request.getAuthorId())));

        setDate(row, cell, request.getIssueDate(), dateStyle);
    }

    private void setDate(Row row, int index, LocalDateTime dt, CellStyle style) {
        Cell cell = row.createCell(index);
        if (dt != null) {
            cell.setCellValue(java.sql.Timestamp.valueOf(dt));
            cell.setCellStyle(style);
        }
    }

    private List<Location> orderedLocations(List<Long> ids, Map<Long, Location> byId) {
        if (ids == null || ids.isEmpty() || byId == null || byId.isEmpty()) {
            return List.of();
        }

        List<Location> result = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Location loc = byId.get(id);
            if (loc != null) {
                result.add(loc);
            }
        }
        return result;
    }

    private String formatLocations(List<Location> locations) {
        if (locations == null || locations.isEmpty()) return "";

        return locations.stream()
                .map(Location::toString)
                .collect(Collectors.joining(" + "));
    }

    private String formatIsoList(List<Location> locations) {
        if (locations == null || locations.isEmpty()) return "";

        return locations.stream()
                .map(Location::getCountryCode)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" + "));
    }

    private String getReason(Request request) {
        return request.getRefuseReason() != null ? request.getRefuseReason() : "";
    }

    private static String nzs(String v) {
        return v == null ? "" : v;
    }

    private static String nzs(UserNameDto v) {
        return v == null ? "" : v.fullName();
    }
}

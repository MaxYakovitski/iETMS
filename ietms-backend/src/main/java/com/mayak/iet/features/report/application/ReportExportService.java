package com.mayak.iet.features.report.application;

import com.mayak.iet.features.report.infra.excel.UserStatsExcelHeader;
import com.mayak.iet.features.report.infra.excel.UserStatsExcelRowMapper;
import com.mayak.iet.statistics.UserStatsDto;
import com.mayak.iet.user.dto.UserNameDto;
import com.mayak.iet.features.location.domain.model.Location;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.features.location.infra.persistence.LocationRepository;
import com.mayak.iet.features.user.infra.persistence.UserRepository;
import com.mayak.iet.features.report.infra.excel.RequestExcelHeader;
import com.mayak.iet.features.report.infra.excel.RequestExcelRowMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReportExportService {

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final RequestExcelHeader header = new RequestExcelHeader();
    private final RequestExcelRowMapper rowMapper = new RequestExcelRowMapper();

    public void exportRequestsToExcel(List<Request> requests, OutputStream out) throws IOException {
        Objects.requireNonNull(out, "OutputStream must not be null");

        List<Request> safeRequests = (requests == null) ? List.of() : requests;
        Map<Long, Location> locationById = preloadLocations(safeRequests);
        Map<Long, UserNameDto> userNameById = preloadUserNames(safeRequests);

        try (XSSFWorkbook workbook = new XSSFWorkbook();){
            XSSFSheet sheet = workbook.createSheet("Requests");
            CellStyle dateStyle = createDateStyle(workbook);

            header.writeHeader(sheet);

            int rowIndex = 1;
            for (Request r : safeRequests) {
                rowMapper.writeRow(sheet, rowIndex++, r, dateStyle, locationById, userNameById);
            }

            for (int i = 0; i <= 22; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);

        }
    }

    public void exportUserStatsToExcel(List<UserStatsDto> stats, OutputStream out) throws IOException {
        Objects.requireNonNull(out, "OutputStream must not be null");

        List<UserStatsDto> safeStats = stats == null ? List.of() : stats;

        var header = new UserStatsExcelHeader();
        var rowMapper = new UserStatsExcelRowMapper();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Employees");

            header.writeHeader(sheet);

            int row = 1;
            for (UserStatsDto dto : safeStats) {
                rowMapper.writeRow(sheet, row++, dto);
            }

            for (int i = 0; i < header.columnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private Map<Long, Location> preloadLocations(List<Request> requests) {
        Set<Long> ids = requests.stream()
                .flatMap(r -> Stream.concat(
                        safeList(r.getFromLocationIds()).stream(),
                        safeList(r.getToLocationIds()).stream()
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (ids.isEmpty()) {
            return Map.of();
        }

        List<Location> locations = locationRepository.findAllById(ids);
        return locations.stream()
                .filter(l -> l.getId() != null)
                .collect(Collectors.toMap(
                        Location::getId,
                        Function.identity(),
                        (a, b) -> a,
                        HashMap::new
                ));
    }

    private Map<Long, UserNameDto> preloadUserNames(List<Request> requests) {

        Set<Long> userIds = requests.stream()
                .flatMap(r -> Stream.of(
                        r.getAuthorId(),
                        r.getDispatcherId()
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> new UserNameDto(u.getName(), u.getSurname())
                ));
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        dateStyle.setDataFormat(helper.createDataFormat().getFormat("dd-MM-yyyy HH:mm"));
        return dateStyle;
    }

    private static <T> List<T> safeList(List<T> v) {
        return v == null ? List.of() : v;
    }
}
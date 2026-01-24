package com.mayak.iet.features.report.infra.excel;

import com.mayak.iet.statistics.UserStatsDto;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class UserStatsExcelRowMapper {

    public void writeRow(Sheet sheet, int rowIndex, UserStatsDto dto) {
        Row row = sheet.createRow(rowIndex);

        int c = 0;
        row.createCell(c++).setCellValue(dto.name().fullName());
        row.createCell(c++).setCellValue(dto.placed());
        row.createCell(c++).setCellValue(dto.joined());
        row.createCell(c++).setCellValue(dto.bided());
        row.createCell(c++).setCellValue(dto.dispatched());
        row.createCell(c).setCellValue(dto.avgResponseMinutes().doubleValue());
    }
}

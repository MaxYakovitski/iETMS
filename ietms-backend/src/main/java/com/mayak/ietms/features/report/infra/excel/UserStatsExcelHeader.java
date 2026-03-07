package com.mayak.ietms.features.report.infra.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class UserStatsExcelHeader {

    private static final String[] HEADERS = {
            "user", "placed", "joined", "bided", "accepted", "dispatched", "avg. response (min)"
    };

    public void writeHeader(Sheet sheet) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < HEADERS.length; i++) {
            row.createCell(i).setCellValue(HEADERS[i]);
        }
    }

    public int columnCount() {
        return HEADERS.length;
    }
}

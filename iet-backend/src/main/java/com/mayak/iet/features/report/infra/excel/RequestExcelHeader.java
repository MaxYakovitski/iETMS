package com.mayak.iet.features.report.infra.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class RequestExcelHeader {
    private static final String[] HEADERS = {
            "ID", "Type","isoFrom", "isoTo","FROM", "TO", "Company", "Start", "End", "Shipment Type", "Transport Type",
            "ADR", "Temp, °C", "Weight, kg", "LDM", "Status", "Reason","Client Price", "Best Bid", "Profit, EUR",
            "Assigned To", "Author", "Issue Date"
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
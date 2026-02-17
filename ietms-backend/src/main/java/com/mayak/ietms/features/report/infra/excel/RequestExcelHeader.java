package com.mayak.ietms.features.report.infra.excel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class RequestExcelHeader {
    private static final String[] HEADERS = {
            "ID", "request","code from", "code to","FROM", "TO", "customer", "start", "end", "shipment Type", "transport Type",
            "ADR", "temp, °C", "weight, kg", "LDM", "status", "reason","client price", "best Bid", "profit, EUR",
            "dispatched to", "author", "issue Date"
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
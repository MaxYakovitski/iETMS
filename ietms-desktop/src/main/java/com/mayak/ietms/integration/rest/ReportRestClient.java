package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.api.ReportClient;
import com.mayak.ietms.statistics.ReportType;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class ReportRestClient extends AbstractRestClient implements ReportClient {

    private static final String API = "/api/reports/requests.xlsx?type={type}&from={from}&to={to}";

    public ReportRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor, SessionManager sessionManager) {
        super(restTemplate, connectionMonitor, sessionManager);
    }

    @Override
    public void downloadRequestsReport(@Qualifier("reportRestTemplate") ReportType type, LocalDate from, LocalDate to, File targetFile) {
        exchangeSafely(() -> {

            byte[] bytes = restTemplate.getForObject(API, byte[].class, type.name(), from.toString(), to.toString());

            if (bytes == null) {
                throw new RuntimeException("Empty report");
            }

            try (var out = new FileOutputStream(targetFile)) {
                out.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save report file", e);
            }

            return null;
        });
    }
}
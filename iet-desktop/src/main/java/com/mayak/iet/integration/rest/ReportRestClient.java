package com.mayak.iet.integration.rest;

import com.mayak.iet.integration.api.ReportClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class ReportRestClient extends AbstractRestClient implements ReportClient {

    private static final String URL =
            "http://localhost:8080/api/reports/requests.xlsx?from={from}&to={to}";

    public ReportRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public void downloadRequestsReport(
            LocalDate from,
            LocalDate to,
            File targetFile
    ) {
        exchangeSafely(() -> {

            byte[] bytes = restTemplate.getForObject(
                    URL,
                    byte[].class,
                    from.toString(),
                    to.toString()
            );

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
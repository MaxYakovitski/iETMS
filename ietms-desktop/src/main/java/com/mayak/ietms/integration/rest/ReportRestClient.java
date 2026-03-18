package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.api.ReportClient;
import com.mayak.ietms.statistics.ReportType;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.function.Consumer;

@Service
public class ReportRestClient extends AbstractRestClient implements ReportClient {

    private static final String API = "/api/reports/requests.xlsx?type={type}&from={from}&to={to}";

    public ReportRestClient(@Qualifier("reportRestTemplate") RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor, SessionManager sessionManager) {
        super(restTemplate, connectionMonitor, sessionManager);
    }

    @Override
    public void downloadRequestsReport(ReportType type, LocalDate from, LocalDate to, File targetFile, Consumer<Double> onProgress) {
        exchangeSafely(() -> {
            restTemplate.execute(
                    API,
                    HttpMethod.GET,
                    null,
                    response -> {
                        long contentLength = response.getHeaders().getContentLength();
                        try (InputStream in = response.getBody();
                             FileOutputStream out = new FileOutputStream(targetFile)) {

                            byte[] buffer = new byte[8192];
                            long totalRead = 0;
                            int bytesRead;

                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                                totalRead += bytesRead;

                                if (contentLength > 0 && onProgress != null) {
                                    onProgress.accept((double) totalRead / contentLength);
                                }
                            }
                        }
                        return null;
                    },
                    type.name(), from.toString(), to.toString()
            );
            return null;
        });
    }
}
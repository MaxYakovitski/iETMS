package com.mayak.ietms.infrastructure.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class SlackErrorReporter {

    private final String webhookUrl;
    private final HttpClient httpClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    public SlackErrorReporter(@Value("${slack.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    public void report(Exception e, String contextInfo) {
        if (!isConfigured()) {
            log.warn("Slack webhook not configured, skipping error report");
            return;
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String stackTrace = getStackTrace(e);

        String text = "*🚨 Desktop Error*\n" +
                "`Time:` " + timestamp + "\n" +
                (contextInfo != null && !contextInfo.isBlank() ? "`Context:` " + contextInfo + "\n" : "") +
                "`Exception:` " + e.getClass().getSimpleName() + "\n" +
                "```" + stackTrace + "```";

        sendToSlack(text);
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private void sendToSlack(String message) {
        try {
            String payload = "{ \"text\": \"" + message.replace("\"", "\\\"") + "\" }";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() != 200) {
                            log.warn("Slack webhook failed: {}", response.body());
                        }
                    });
        } catch (Exception ex) {
            log.error("Failed to send error to Slack", ex);
        }
    }
}

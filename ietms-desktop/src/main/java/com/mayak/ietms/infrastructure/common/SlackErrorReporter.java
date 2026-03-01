package com.mayak.ietms.infrastructure.common;

import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class SlackErrorReporter {

    private final String webhookUrl;
    private final HttpClient httpClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");

    public SlackErrorReporter(String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void report(Exception e, String contextInfo) {
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
                            System.err.println("Slack webhook failed: " + response.body());
                        }
                    });
        } catch (Exception ex) {
            log.error("Failed to send error to Slack", ex);
        }
    }
}

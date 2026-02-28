package com.mayak.ietms.infrastructure.notify;

import com.mayak.ietms.infrastructure.config.SlackProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public final class SlackNotifier {

    private final SlackProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendError(String message) {
        String webhookUrl = properties.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {

            String payload = """
                    {
                      "text": "%s"
                    }
                    """.formatted(message.replace("\"", "'"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Slack webhook error: status={}, body={}",
                        response.statusCode(),
                        response.body());
            }

        } catch (Exception e) {
            log.error("Failed to send Slack notification", e);
        }
    }
}
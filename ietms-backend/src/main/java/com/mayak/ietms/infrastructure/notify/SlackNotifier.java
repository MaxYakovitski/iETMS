package com.mayak.ietms.infrastructure.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.ietms.infrastructure.config.SlackProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Sends error notifications to a configured Slack webhook.
 *
 * <p>If the webhook URL is not configured, notifications are silently skipped.
 * All errors during delivery are caught and logged to avoid disrupting
 * the main application flow.
 *
 * <p>Uses {@link ObjectMapper} for safe JSON serialization, preventing
 * payload corruption from special characters in stack traces.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class SlackNotifier {

    private final SlackProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Sends an error message to the configured Slack webhook.
     *
     * <p>If the webhook URL is blank or the request fails, the error
     * is logged and the method returns silently without throwing.
     *
     * @param message the error message to send; may contain special characters
     */
    public void sendError(String message) {
        String webhookUrl = properties.getWebhookUrl();
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {

            String payload = objectMapper.writeValueAsString(Map.of("text", message));

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
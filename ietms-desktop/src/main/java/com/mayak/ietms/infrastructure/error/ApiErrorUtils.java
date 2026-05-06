package com.mayak.ietms.infrastructure.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.ietms.integration.exception.ApiException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for converting {@link ApiException} instances into user-facing {@link UiError} messages.
 */
@UtilityClass
@Slf4j
public class ApiErrorUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Resolves an {@link ApiException} into a {@link UiError} with a user-friendly message.
     * Falls back to {@code defaultMessage} when no specific message can be extracted.
     *
     * @param e              the exception to resolve, may be {@code null}
     * @param defaultMessage fallback message used when the exception provides no useful detail
     * @return a {@link UiError} with appropriate severity and message
     */
    public UiError resolve(ApiException e, String defaultMessage) {
        if (e == null) return UiError.error(defaultMessage);

        int status = e.getStatus() != null ? e.getStatus().value() : -1;
        String backendMessage = extractMessageFromBody(e.getMessage());

        return switch (status) {
            case 400 -> UiError.error(
                    backendMessage != null
                    ? backendMessage : "Invalid request data.");
            case 401 -> UiError.error(backendMessage != null
                    ? backendMessage : "You are not authorized to perform this action.");
            case 403 -> UiError.error("You do not have permission to perform this action.");
            case 404 -> UiError.warning(backendMessage != null
                    ? backendMessage
                    : defaultMessage != null
                    ? defaultMessage
                    : "Requested resource was not found.");
            case 409 -> UiError.warning( backendMessage != null
                    ? backendMessage
                    : defaultMessage != null
                    ? defaultMessage
                    : "Operation conflict.");
            case 500 -> UiError.error(defaultMessage != null
                    ? defaultMessage : "Server error occurred. Please try again later.");
            default -> UiError.error(defaultMessage != null
                    ? defaultMessage : "Unexpected error occurred.");
        };
    }

    private String extractMessageFromBody(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            JsonNode node = MAPPER.readTree(raw);
            if (node.hasNonNull("message")) {
                return node.get("message").asText();
            }
        } catch (Exception ex) {
            log.debug("Failed to parse ApiException body as JSON");
        }

        return null;
    }
}
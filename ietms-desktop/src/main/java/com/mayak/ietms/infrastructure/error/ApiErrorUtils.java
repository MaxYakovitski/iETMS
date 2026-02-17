package com.mayak.ietms.infrastructure.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.ietms.integration.exception.ApiException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class ApiErrorUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public UiError resolve(ApiException e, String defaultMessage) {

        if (e == null) {
            return UiError.error(defaultMessage);
        }

        int status = e.getStatus() != null ? e.getStatus().value() : -1;
        String backendMessage = extractMessageFromBody(e.getMessage());

        return switch (status) {
            case 400 -> UiError.error(backendMessage != null ? backendMessage : "Invalid request data.");
            case 401 -> UiError.error(backendMessage != null ? backendMessage : "You are not authorized to perform this action.");
            case 403 -> UiError.error("You do not have permission to perform this action.");
            case 404 -> UiError.warning(defaultMessage != null ? defaultMessage : "Requested resource was not found.");
            case 409 -> UiError.warning( backendMessage != null
                    ? backendMessage
                    : defaultMessage != null
                    ? defaultMessage
                    : "Operation conflict.");
            case 500 -> UiError.error(defaultMessage != null ? defaultMessage : "Server error occurred. Please try again later.");
            default -> UiError.error(defaultMessage != null ? defaultMessage : "Unexpected error occurred.");
        };
    }

    /** Backward compatibility / convenience */
    public String resolveUserMessage(ApiException e, String defaultMessage) {
        return resolve(e, defaultMessage).message();
    }

    /* ================== INTERNAL ================== */

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
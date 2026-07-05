package com.mayak.ietms.infrastructure.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.integration.exception.NetworkUnavailableException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

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
     * @return a {@link UiError} with appropriate severity and message
     */
    public UiError resolve(Throwable ex) {
        Throwable cause = unwrapAsync(ex);
        if (cause instanceof ApiException apiException) {
            return resolveApiException(apiException);
        }
        if (cause instanceof NetworkUnavailableException) {
            return UiError.error("No internet connection or cannot reach the server.\nPlease check your network!");
        }
        log.error("[api] Unexpected error", ex);
        return UiError.error("Unexpected error occurred. Please try again.");
    }

    private Throwable unwrapAsync(Throwable ex) {
        if ((ex instanceof CompletionException || ex instanceof ExecutionException) && ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    private static UiError resolveApiException(ApiException apiException) {
        String backendMessage = extractMessageFromBody(apiException.getMessage());
        int status = apiException.getStatus() != null ? apiException.getStatus().value() : -1;
        boolean warning = status == 404 || status == 409;

        if (backendMessage != null) {
            return warning ? UiError.warning(backendMessage) : UiError.error(backendMessage);
        }

        return switch (status) {
            case 400 -> UiError.error("Invalid request data.");
            case 401 -> UiError.error("You are not authorized to perform this action.");
            case 403 -> UiError.error("You do not have permission to perform this action.");
            case 404 -> UiError.warning("Requested resource was not found.");
            case 409 -> UiError.warning( "Operation conflict.");
            case 500 -> UiError.error("Server error occurred. Please try again later.");
            default -> UiError.error("Unexpected error occurred.");
        };

    }

    private String extractMessageFromBody(String raw) {
        if (raw == null || raw.isBlank()) return null;
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

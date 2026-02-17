package com.mayak.ietms.extension.event;

import com.mayak.ietms.extension.dto.DraftValidationErrorResponse;

import java.time.Instant;

public record ExtensionDraftInvalidEvent(Long userId, DraftValidationErrorResponse payload, Instant occurredAt) {

    public static ExtensionDraftInvalidEvent of(Long userId, DraftValidationErrorResponse payload) {
        return new ExtensionDraftInvalidEvent(userId, payload, Instant.now());
    }
}
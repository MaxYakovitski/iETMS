package com.mayak.iet.extension.event;

import com.mayak.iet.extension.dto.DraftValidationErrorResponse;

import java.time.Instant;

public record ExtensionDraftInvalidEvent(Long userId, DraftValidationErrorResponse payload, Instant occurredAt) {

    public static ExtensionDraftInvalidEvent of(Long userId, DraftValidationErrorResponse payload) {
        return new ExtensionDraftInvalidEvent(userId, payload, Instant.now());
    }
}
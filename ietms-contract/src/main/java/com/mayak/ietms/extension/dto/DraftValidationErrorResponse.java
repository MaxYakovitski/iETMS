package com.mayak.ietms.extension.dto;

import java.util.List;
import java.util.Map;

public record DraftValidationErrorResponse (
        ExtensionDraftIntent intent,
        Map<String, List<String>> errors) implements ExtensionDraftResponse {
}
package com.mayak.ietms.extension.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DraftValidationErrorResponse (
        Map<String, List<String>> errors) implements ExtensionDraftResponse {
}
package com.mayak.iet.integration.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mayak.iet.common.dto.error.ErrorResponseDto;
import lombok.Getter;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

@Getter
public class ApiValidationException extends RuntimeException {

    private final Map<String, String> errors;

    public ApiValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

    public static ApiValidationException fromResponse(HttpStatusCodeException ex) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponseDto response = mapper.readValue(ex.getResponseBodyAsString(), ErrorResponseDto.class);
            return new ApiValidationException(response.message(), response.errors());

        } catch (Exception e) {
            return new ApiValidationException(
                    "Validation failed",
                    Map.of("_global", "Invalid request data")
            );
        }
    }
}
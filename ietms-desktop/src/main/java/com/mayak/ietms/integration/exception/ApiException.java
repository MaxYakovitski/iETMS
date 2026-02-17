package com.mayak.ietms.integration.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatusCode status;

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = null;
    }

    public ApiException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

}
package com.mayak.ietms.infrastructure.web.exception;

import com.mayak.ietms.common.dto.error.ErrorResponseDto;
import com.mayak.ietms.infrastructure.notify.SlackAlertService;
import com.mayak.ietms.shared.exception.business.*;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.common.validation.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Maps domain and application exceptions to appropriate HTTP responses.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiExceptionHandler {

    private final SlackAlertService slackAlertService;

    @ExceptionHandler({
            UserNotFoundException.class,
            CompanyNotFoundException.class,
            DepartmentNotFoundException.class,
            RequestNotFoundException.class,
            ShipmentNotFoundException.class,
            LaneNotFoundException.class,
            LocationNotFoundException.class,
            BidNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotFound(RuntimeException ex) {
        return new ErrorResponseDto("not_found", ex.getMessage());
    }

    @ExceptionHandler({
            UserAlreadyExistsException.class,
            CompanyAlreadyExistsException.class,
            DepartmentAlreadyExistsException.class,
            LaneAlreadyExistsException.class,
            LocationAlreadyExistsException.class,
            NoActiveBidsException.class,
            ShipmentCancellationNotAllowedException.class,
            RequestDeletionNotAllowedException.class,
            DeliveryTimeLineException.class,
            InvalidShipmentStatusTransitionException.class,
            LaneInUseException.class,
            UserInUseException.class,
            DepartmentInUseException.class,
            LocationInUseException.class,
            RequestStateException.class,
            CompanyInUseException.class,
            AlreadyJoinedException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleConflict(RuntimeException ex) {
        return new ErrorResponseDto("conflict", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto handleForbidden(UnauthorizedException ex) {
        return new ErrorResponseDto("forbidden", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto handleAccessDenied(HttpServletRequest request) {
        log.warn("Access denied: {} {}", request.getMethod(), request.getRequestURI());
        return new ErrorResponseDto("forbidden", "Access denied");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto handleUnauthorized(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {} {} — {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return new ErrorResponseDto("unauthorized", ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatus(ResponseStatusException ex,  HttpServletRequest request) {
        log.warn("Request rejected: {} {} — {}", request.getMethod(), request.getRequestURI(), ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).build();
    }

    @ExceptionHandler(LicenseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleLicense(LicenseException ex) {
        return new ErrorResponseDto("license_not_found", ex.getMessage());
    }

    @ExceptionHandler(LicenseLimitExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleLicenseLimitExceeded(LicenseLimitExceededException ex) {
        return new ErrorResponseDto("license_limit_exceeded", ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidation(ValidationException ex) {
        Map<String, String> errors = ex.getResult().getErrors().stream()
                .collect(Collectors.toMap(
                        ValidationError::code,
                        ValidationError::message,
                        (a, b) -> a));
        return new ErrorResponseDto("validation_error","Validation failed", errors);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
        log.debug("Client disconnected before response was sent: {}", ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNoResourceFound(NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
        return new ErrorResponseDto("not_found", "Resource not found");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ErrorResponseDto handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return new ErrorResponseDto("method_not_allowed", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleBadRequest() {
        return new ErrorResponseDto("bad_request", "Request body is missing or malformed");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        slackAlertService.sendHttpError(ex, request);
        return new ErrorResponseDto("internal_error", "Internal server error");
    }
}
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

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
    public ApiError handleNotFound(RuntimeException ex) {
        return new ApiError("not_found", ex.getMessage());
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
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(RuntimeException ex) {
        return new ApiError("conflict", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(UnauthorizedException ex) {
        return new ApiError("forbidden", ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorized(AuthenticationException ex) {
        return new ApiError("unauthorized", ex.getMessage());
    }

    @ExceptionHandler(LicenseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleLicense(LicenseException ex) {
        return new ApiError("license_not_found", ex.getMessage());
    }

    @ExceptionHandler(LicenseLimitExceededException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleLicenseLimitExceeded(LicenseLimitExceededException ex) {
        return new ApiError("license_limit_exceeded", ex.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidation(ValidationException ex) {
        Map<String, String> errors = ex.getResult().getErrors().stream()
                .collect(Collectors.toMap(
                        ValidationError::code,
                        ValidationError::message,
                        (a, b) -> a));

        return new ErrorResponseDto("Validation failed", errors);
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException ex) {
        log.debug("Client disconnected before response was sent: {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error", ex);
        slackAlertService.sendHttpError(ex, request);
        return new ApiError("internal_error", "Internal server error");
    }
}
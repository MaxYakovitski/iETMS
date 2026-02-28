package com.mayak.ietms.infrastructure.web.exception;

import com.mayak.ietms.common.dto.error.ErrorResponseDto;
import com.mayak.ietms.infrastructure.notify.SlackNotifier;
import com.mayak.ietms.shared.exception.business.*;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.common.validation.ValidationError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final SlackNotifier slackNotifier;
    private final Environment environment;

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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception ex, HttpServletRequest request) {

        log.error("Unexpected error", ex);
        slackNotifier.sendError(buildSlackMessage(ex, request)
        );

        return new ApiError("internal_error", "Internal server error");
    }

    private String buildSlackMessage(Exception ex, HttpServletRequest request) {

        String profile = Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .orElse("unknown")
                .toUpperCase();

        String host = getHostname();

        String stack = Arrays.stream(ex.getStackTrace())
                .limit(5)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));

        return """
                🚨 *Backend error (%s)*

                Host: %s
                URI: %s %s

                Type: %s
                Message: %s

                Stack:
                %s
                """
                .formatted(
                        profile,
                        host,
                        request.getMethod(),
                        request.getRequestURI(),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        stack
                );
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
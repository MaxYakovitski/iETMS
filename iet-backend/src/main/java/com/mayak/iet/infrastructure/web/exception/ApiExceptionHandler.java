package com.mayak.iet.infrastructure.web.exception;

import com.mayak.iet.common.dto.error.ErrorResponseDto;
import com.mayak.iet.shared.exception.business.*;
import com.mayak.iet.shared.exception.validation.ValidationException;
import com.mayak.iet.common.validation.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

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
    public ApiError handleUnexpected(Exception ex) {
        return new ApiError("internal_error", ex.getMessage());
    }
}
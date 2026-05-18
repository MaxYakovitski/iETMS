package com.mayak.ietms.features.extension.api;

import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.extension.dto.*;
import com.mayak.ietms.features.extension.application.ExtensionRequestAssembler;
import com.mayak.ietms.features.request.application.lifecycle.RequestLifecycleService;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/extension")
@Tag(name = "Extension", description = "Browser extension integration endpoints")
@RequiredArgsConstructor
@Slf4j
public class ExtensionRequestDraftController {

    private final ExtensionRequestAssembler assembler;
    private final RequestLifecycleService requestLifecycleService;

    @PostMapping("/request")
    @Operation(summary = "Submit request draft from browser extension",
               description = "Accepts a draft request from the browser extension. " +
                             "Returns 200 on success, 422 with field errors if validation fails."
    )
    public ResponseEntity <ExtensionDraftResponse> create(@RequestBody ExtensionRequestDraftDto draft, @CurrentUserId Long userId) {
        try {
            BaseRequestDto requestDto = assembler.build(draft);
            requestLifecycleService.create(requestDto, userId);
            return ResponseEntity.ok(new DraftAcceptedResponse());
        } catch (ValidationException ex) {
            Map<String, List<String>> errors = toErrorMap(ex.getResult().getErrors());
            return ResponseEntity
                    .unprocessableEntity()
                    .body(new DraftValidationErrorResponse(errors));
        }

    }

    private Map<String, List<String>> toErrorMap(List<ValidationError> errors) {
        return errors.stream().collect(Collectors.groupingBy(
                        ValidationError::code,
                        Collectors.mapping(ValidationError::message, Collectors.toList())));
    }
}
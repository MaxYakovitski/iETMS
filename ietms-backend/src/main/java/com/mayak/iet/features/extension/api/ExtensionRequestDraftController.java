package com.mayak.iet.features.extension.api;

import com.mayak.iet.common.validation.ValidationError;
import com.mayak.iet.extension.dto.*;
import com.mayak.iet.extension.event.ExtensionDraftInvalidEvent;
import com.mayak.iet.features.extension.application.ExtensionRequestAssembler;
import com.mayak.iet.features.extension.notify.ExtensionNotificationService;
import com.mayak.iet.features.request.application.lifecycle.RequestLifecycleService;
import com.mayak.iet.infrastructure.security.current.CurrentUserId;
import com.mayak.iet.request.dto.create.BaseRequestDto;
import com.mayak.iet.shared.exception.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/extension")
@RequiredArgsConstructor
@Slf4j
public class ExtensionRequestDraftController {

    private final ExtensionRequestAssembler assembler;
    private final RequestLifecycleService requestLifecycleService;
    private final ExtensionNotificationService extensionNotificationService;

    @PostMapping("/request")
    public ExtensionDraftResponse create(@RequestBody ExtensionRequestDraftDto draft, @CurrentUserId Long userId) {
        ExtensionDraftIntent intent = ExtensionDraftIntent.from(draft);

        try {
            BaseRequestDto requestDto = assembler.build(draft);
            requestLifecycleService.create(requestDto, userId);
            return new DraftAcceptedResponse();

        } catch (ValidationException ex) {
            DraftValidationErrorResponse response =
                    new DraftValidationErrorResponse(intent, toErrorMap(ex.getResult().getErrors()));

            extensionNotificationService.publishDraftInvalid(ExtensionDraftInvalidEvent.of(userId, response));

            return response;
        }

    }

    private Map<String, List<String>> toErrorMap(List<ValidationError> errors) {
        return errors.stream().collect(Collectors.groupingBy(
                        ValidationError::code,
                        Collectors.mapping(ValidationError::message, Collectors.toList())));
    }
}
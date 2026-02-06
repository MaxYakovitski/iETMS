package com.mayak.iet.ui.workspace.request.client;

import com.mayak.iet.common.validation.ValidationError;
import com.mayak.iet.domain.request.client.ClientRequestPolicy;
import com.mayak.iet.extension.event.ExtensionDraftInvalidEvent;
import com.mayak.iet.ui.workspace.request.form.ClientRequestFormState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ClientRequestExtensionHandler {

    private final ClientRequestPolicy requestPolicy;
    private final ClientRequestFormState requestState;

    public void handle(ExtensionDraftInvalidEvent event, Runnable render, Consumer<List<ValidationError>> showErrors) {
        if (event == null || event.payload() == null || event.payload().intent() == null) {
            log.warn("Extension event without valid draft payload");
            return;
        }

        var payload = event.payload();
        requestPolicy.applyDraft(requestState, payload.intent());
        render.run();

        List<ValidationError> errors = mapErrors(payload.errors());
        if (!errors.isEmpty()) {
            showErrors.accept(errors);
        }

    }

    private List<ValidationError> mapErrors(Map<String, List<String>> errors) {
        if (errors == null || errors.isEmpty()) {
            return List.of();
        }

        return errors.entrySet().stream()
                .flatMap(e -> e.getValue().stream()
                        .map(msg -> new ValidationError(e.getKey(), msg)))
                .toList();
    }
}

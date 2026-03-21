package com.mayak.ietms.ui.workspace.request.client;

import com.mayak.ietms.common.validation.ValidationError;
import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.domain.request.client.ClientRequestPolicy;
import com.mayak.ietms.infrastructure.common.TextUtils;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.error.ApiErrorUtils;
import com.mayak.ietms.infrastructure.ui.ValidationUIHelper;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.integration.api.CompanyClient;
import com.mayak.ietms.integration.api.LaneClient;
import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.lane.dto.LaneViewDto;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.component.LaneSelectorController;
import com.mayak.ietms.ui.workspace.request.form.ClientRequestFormState;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class ClientCompanyLaneCoordinator {

    private final CompanyClient companyClient;
    private final LaneClient laneClient;
    private final WindowService windowService;

    private final ClientRequestPolicy requestPolicy;
    private final ClientRequestFormState requestState;
    private final ValidationUIHelper validationUI;
    private final Runnable render;
    private final Runnable onLaneChanged;

    private final Supplier<Boolean> allowLaneLookup;
    private final Supplier<String> companyNameSupplier;

    private static final String CONTACT_LANE = "/icons/contract-lanes.png";

    public void onCompanyConfirmed() {
        if (!allowLaneLookup.get()) return;
        if (!requestState.isContract()) return;

        String value = TextUtils.safeTrim(companyNameSupplier.get());
        if (value == null) {
            validationUI.showClientErrors(List.of(new ValidationError("customerName", "Customer must be selected for contract request")));
            return;
        };
        CompletableFuture.runAsync(() -> {
            try {
                Optional<CompanyDto> company = companyClient.findByName(value);
                Platform.runLater(() -> {
                            company.ifPresentOrElse(
                                    this::loadLanesAsync,
                                    () -> AlertUtils.showError("Company not found."));
                        }
                );

            } catch (ApiException ex) {
                log.warn("Company lookup failed", ex);
                Platform.runLater(() -> {
                    AlertUtils.show(ApiErrorUtils.resolve(ex, "Failed to find company."));
                });
            }
        });
    }

    private void loadLanesAsync(CompanyDto company) {
        CompletableFuture.runAsync(() -> {
            try {
                List<LaneViewDto> lanes = laneClient.findByCompany(company.id());
                Platform.runLater(() -> {
                    if (lanes.isEmpty()) {
                        AlertUtils.showError("No available lanes for this customer.");
                        return;
                    }
                    showLaneSelector(lanes);
                });
            } catch (ApiException ex) {
                log.warn("Lane lookup failed", ex);
                Platform.runLater(() ->
                        AlertUtils.show(ApiErrorUtils.resolve(ex, "Failed to load lanes."))
                );
            }
        });
    }

    private void showLaneSelector(List<LaneViewDto> lanes) {
        LaneSelectorController ctrl = windowService.openModalAndWait(
                View.LANES.getPath(),
                LaneSelectorController.class,
                controller -> {
                    controller.setLanes(lanes);
                    controller.onShown();
                },
                "Actual contract lanes",
                CONTACT_LANE
        );

        LaneViewDto lane = ctrl.getSelectedLane();
        if (lane == null) return;

        requestPolicy.onLaneSelected(requestState, lane);
        requestState.setStartDate(null);
        requestState.setEndDate(null);
        validationUI.clearError("lane");
        onLaneChanged.run();
        render.run();
    }
}
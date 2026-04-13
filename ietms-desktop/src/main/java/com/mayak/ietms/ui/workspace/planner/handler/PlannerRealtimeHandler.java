package com.mayak.ietms.ui.workspace.planner.handler;

import com.mayak.ietms.infrastructure.fx.CompanyEventHandler;
import com.mayak.ietms.infrastructure.toast.ToastService;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.integration.websocket.CompanyStompClient;
import com.mayak.ietms.integration.websocket.ShipmentStompClient;
import com.mayak.ietms.request.dto.event.ShipmentEventDto;
import com.mayak.ietms.shipment.dto.enums.ShipmentStatusDto;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class PlannerRealtimeHandler {

    private final CompanyStompClient companyStompClient;
    private final ShipmentStompClient shipmentStompClient;
    private final WindowService windowService;

    /**
     * Subscribes to shipment and company WebSocket streams.
     *
     * @return unsubscribe callback for the company stream — must be called on view hide
     */
    public Runnable init(Set<String> companySuggestions, Runnable onStatusChanged, Consumer<Long> onInvalidate) {

        shipmentStompClient.connect(event -> Platform.runLater(() -> {
            handleShipmentEvent(event, onStatusChanged, onInvalidate);
            handleShipmentUserEvent(event);
        }));

        return companyStompClient.connect(event ->
                Platform.runLater(() -> CompanyEventHandler.apply(event, companySuggestions)));
    }

    private void handleShipmentEvent(ShipmentEvent<ShipmentEventDto> event, Runnable onStatusChanged, Consumer<Long> onInvalidate) {
        if (event == null) return;

        switch (event.getType()) {
            case STATUS_CHANGED -> onStatusChanged.run();
            case UPDATED        -> onInvalidate.accept(event.getShipmentId());
        }
    }

    private void handleShipmentUserEvent(ShipmentEvent<ShipmentEventDto> event) {
        if (event == null || event.getPayload() == null) return;
        if (event.getType() != ShipmentEvent.EventType.STATUS_CHANGED) return;

        if (ShipmentStatusDto.CANCELED.name().equals(event.getPayload().status())) {
            ToastService.showInfo(
                    windowService.getPrimaryStage(),
                    "Shipment canceled",
                    "Shipment #" + event.getShipmentId() + " has been canceled");
        }
    }
}
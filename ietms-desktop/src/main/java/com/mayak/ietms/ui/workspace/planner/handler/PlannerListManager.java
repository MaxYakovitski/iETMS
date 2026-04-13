package com.mayak.ietms.ui.workspace.planner.handler;

import com.mayak.ietms.domain.planner.service.PlannerDataService;
import com.mayak.ietms.domain.planner.service.PlannerSelectionService;
import com.mayak.ietms.domain.planner.service.ShipmentSortingService;
import com.mayak.ietms.domain.planner.service.TransportSortingService;
import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.ui.workspace.planner.state.PlannerState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlannerListManager {

    private final PlannerDataService dataService;
    private final ShipmentSortingService shipmentSortingService;
    private final TransportSortingService transportSortingService;
    private final PlannerSelectionService selectionService;


    private ObservableList<ShipmentListItemDto> shipmentItems;
    private ObservableList<ShipmentListItemDto> transportItems;
    private PlannerState state;

    private Consumer<ShipmentListItemDto> onShipmentSelected;
    private Consumer<ShipmentListItemDto> onTransportSelected;

    public void init(
            ObservableList<ShipmentListItemDto> shipmentItems,
            ObservableList<ShipmentListItemDto> transportItems,
            PlannerState state,
            Consumer<ShipmentListItemDto> onShipmentSelected,
            Consumer<ShipmentListItemDto> onTransportSelected) {
        this.shipmentItems = shipmentItems;
        this.transportItems = transportItems;
        this.state = state;
        this.onShipmentSelected = onShipmentSelected;
        this.onTransportSelected = onTransportSelected;
    }

    public void applyShipmentUpdate(ShipmentListItemDto fresh) {
        if (fresh == null) return;

        replaceIn(shipmentItems, fresh);
        replaceIn(transportItems, fresh);

        resort(shipmentItems, shipmentSortingService.byDateThenStatus());
        resort(transportItems, transportSortingService.byStatusThenDate());

        if (state.getSelectedShipment() != null && Objects.equals(state.getSelectedShipment().id(), fresh.id())) {
            state.setSelectedShipment(fresh);
            if (selectionService.isShipmentTab(state)) onShipmentSelected.accept(fresh);
            else onTransportSelected.accept(fresh);
        }
    }

    public void invalidateShipment(Long shipmentId) {
        if (shipmentId == null) return;

        CompletableFuture
                .supplyAsync(() -> dataService.loadShipmentById(shipmentId))
                .thenAccept(fresh -> Platform.runLater(() -> {
                    if (fresh == null) return;

                    replaceIn(shipmentItems, fresh);
                    replaceIn(transportItems, fresh);

                    if (state.getSelectedShipment() != null && Objects.equals(state.getSelectedShipment().id(), shipmentId)) {
                        state.setSelectedShipment(fresh);
                        if (selectionService.isShipmentTab(state)) onShipmentSelected.accept(fresh);
                        else onTransportSelected.accept(fresh);
                    }
                }))
                .exceptionally(ex -> {
                    log.warn("Failed to refresh shipment {}", shipmentId, ex);
                    return null;
                });
    }

    private void resort(ObservableList<ShipmentListItemDto> list, Comparator<ShipmentListItemDto> comparator) {
        if (list.size() < 2) return;
        FXCollections.sort(list, comparator);
    }

    private static void replaceIn(ObservableList<ShipmentListItemDto> list, ShipmentListItemDto fresh) {
        Long id = fresh.id();
        if (id == null) return;

        for (int i = 0; i < list.size(); i++) {
            ShipmentListItemDto it = list.get(i);
            if (it != null && Objects.equals(it.id(), id)) {
                list.set(i, fresh);
                return;
            }
        }
    }
}
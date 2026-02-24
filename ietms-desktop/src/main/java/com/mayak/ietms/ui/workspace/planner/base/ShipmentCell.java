package com.mayak.ietms.ui.workspace.planner.base;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.workspace.planner.controller.PlannerController;
import com.mayak.ietms.ui.workspace.planner.enums.ActiveTab;
import com.mayak.ietms.ui.workspace.planner.item.ShipmentItemController;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;

public class ShipmentCell extends ListCell<ShipmentListItemDto> implements ViewLifecycle {

    private final ShipmentItemController controller;
    private final StackPane container;
    private final PlannerController parent;

    private Long currentId;

    public ShipmentCell(WindowService windowService, PlannerController parent) {
        this.parent = parent;

        String fxmlPath = View.SHIPMENT_ITEM.getPath();
        WindowService.Loaded<ShipmentItemController> loaded =
                windowService.loadControllerWithNode(fxmlPath, ShipmentItemController.class);

        controller = loaded.controller();
        controller.onShowOnce();
        container = new StackPane();
        container.getChildren().add(controller.requestPane);
    }

    public void setActiveTab(ActiveTab activeTab) {
        controller.setActiveTab(activeTab);
    }

    @Override
    protected void updateItem(ShipmentListItemDto item, boolean empty) {
        super.updateItem(item, empty);

        if (currentId != null) {
            parent.unregisterVisibleShipment(currentId);
            currentId = null;
        }

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        currentId = item.id();

        controller.updateItem(item);
        parent.registerVisibleShipment(currentId, controller);

        setGraphic(container);
    }
}
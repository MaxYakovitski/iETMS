package com.mayak.iet.ui.workspace.planner.base;

import com.mayak.iet.shipment.dto.view.ShipmentListItemDto;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.workspace.planner.enums.ActiveTab;
import com.mayak.iet.ui.workspace.planner.item.ShipmentItemController;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;

public class ShipmentCell extends ListCell<ShipmentListItemDto> implements ViewLifecycle {

    private final ShipmentItemController controller;
    private final StackPane container;

    public ShipmentCell(WindowService windowService) {
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

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        controller.updateItem(item);
        setGraphic(container);
    }
}
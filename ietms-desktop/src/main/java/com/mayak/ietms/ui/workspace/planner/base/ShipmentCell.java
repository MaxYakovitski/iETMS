package com.mayak.ietms.ui.workspace.planner.base;

import com.mayak.ietms.shipment.dto.view.ShipmentListItemDto;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.workspace.planner.enums.ActiveTab;
import com.mayak.ietms.ui.workspace.planner.item.ShipmentItemController;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;
import net.rgielen.fxweaver.core.FxWeaver;

public class ShipmentCell extends ListCell<ShipmentListItemDto> implements ViewLifecycle {

    private final ShipmentItemController controller;
    private final StackPane container;

    public ShipmentCell(FxWeaver fxWeaver) {
        var loaded = fxWeaver.load(ShipmentItemController.class);
        controller = loaded.getController();
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
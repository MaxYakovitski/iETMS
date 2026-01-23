package com.mayak.iet.ui.workspace.request.base;

import com.mayak.iet.request.dto.view.RequestListItemDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.workspace.request.item.RequestItemController;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;

public class RequestCell extends ListCell<RequestListItemDto> implements ViewLifecycle {

    private final RequestItemController controller;
    private final StackPane container;

    public RequestCell(WindowService windowService, UserResponseDto user, RequestsParent parent) {

        String fxmlPath = View.REQUEST_ITEM.getPath();

        WindowService.Loaded<RequestItemController> loaded =
                windowService.loadControllerWithNode(fxmlPath, RequestItemController.class);

        controller = loaded.controller();
        controller.setContext(user, parent);

        if (parent instanceof AbstractRequestController arc && arc.getStage() != null) {
            controller.setStage(arc.getStage());
        }

        container = new StackPane();
        container.getChildren().add(controller.getRequestPane());
    }

    @Override
    protected void updateItem(RequestListItemDto item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
        } else {
            controller.setRequestId(item.id());
            controller.onShow();
            setGraphic(container);
        }
    }
}
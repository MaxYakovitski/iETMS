package com.mayak.iet.ui.workspace.request.base;

import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.request.dto.view.RequestListItemDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.workspace.request.item.RequestItemController;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.application.Platform;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RequestCell extends ListCell<RequestListItemDto> implements ViewLifecycle {

    private final RequestItemController controller;
    private final StackPane container;
    private final AbstractRequestController parent;

    private Long currentId;

    public RequestCell(WindowService windowService, UserResponseDto user, AbstractRequestController parent) {
        this.parent = parent;
        String fxmlPath = View.REQUEST_ITEM.getPath();

        WindowService.Loaded<RequestItemController> loaded =
                windowService.loadControllerWithNode(fxmlPath, RequestItemController.class);

        controller = loaded.controller();
        controller.setContext(user, parent);

        if (parent.getStage() != null) {
            controller.setStage(parent.getStage());
        }

        container = new StackPane();
        container.getChildren().add(controller.getRequestPane());
    }

    @Override
    protected void updateItem(RequestListItemDto item, boolean empty) {
        super.updateItem(item, empty);

        if (currentId != null) {
            parent.unregisterVisible(currentId);
            currentId = null;
        }

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        Long id = item.id();
        currentId = id;

        controller.bindRequest(id);
        parent.registerVisible(id, controller);

        CompletableFuture<RequestDetailsDto> future = parent.getDetailsAsync(id);

        if (!future.isDone() && controller.isBlank()) controller.renderSkeleton();

        future.thenAccept(dto ->
                Platform.runLater(() -> {
                    if (!Objects.equals(controller.getRequestId(), id)) return;
                    controller.attachDetails(dto);
                })
        );

        setGraphic(container);
    }
}
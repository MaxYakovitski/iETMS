package com.mayak.iet.ui.navigation;

import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.home.HomeController;
import com.mayak.iet.ui.workspace.request.transport.TransportRequestController;
import com.mayak.iet.infrastructure.window.WindowKey;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.scene.layout.AnchorPane;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NavigationService {

    private final WindowService windowService;
    private final HomeController homeController;
    private ViewLifecycle currentInlineView;
    private final UserResponseDto loggedInUser;

    public void navigate(View view, NavigationType type) {
        navigate(view, type, null, ModalOptions.empty());
    }

    public void navigate(View view, NavigationType type, Object payload, ModalOptions options) {
        switch (type) {
            case GLOBAL -> homeController.loadView(view);
            case INLINE -> openInline(view);
            case MODAL -> openModal(view, options);
            case DETACHED -> openDetached(view, payload);
        }
    }

    private void openInline(View view) {
        var loaded = windowService.loadControllerWithNode(view.getPath());
        var controller = loaded.controller();

        if (currentInlineView != null) {
            currentInlineView.onHide();
            currentInlineView = null;
        }

        AnchorPane.setTopAnchor(loaded.node(), 0.0);
        AnchorPane.setBottomAnchor(loaded.node(), 0.0);
        AnchorPane.setLeftAnchor(loaded.node(), 0.0);
        AnchorPane.setRightAnchor(loaded.node(), 0.0);

        homeController.getContentArea().getChildren().setAll(loaded.node());

        if (controller instanceof ViewLifecycle lifecycle) {
            lifecycle.onShow();
            currentInlineView = lifecycle;
        }
    }

    private void openModal(View view, ModalOptions options) {
        windowService.openModalWindow(
                view.getPath(),
                Object.class,
                c -> {},
                options.title() != null ? options.title() : view.name(),
                options.iconPath()
        );
    }

    private void openDetached(View view, Object payload) {

        if (view != View.REQUESTS_TRANSPORT) {
            throw new IllegalStateException(
                    "DETACHED navigation is allowed only for transport requests"
            );
        }

        if (!(payload instanceof RequestTypeDto type)) {
            throw new IllegalArgumentException(
                    "Transport detached navigation requires RequestTypeDto payload"
            );
        }

            windowService.openDetachedWindow(
                    view.getPath(),
                    TransportRequestController.class,
                    controller -> {
                        controller.setRequestType(type);
                        controller.setHomeController(homeController);
                        controller.setLoggedInUser(loggedInUser);
                    },
                    buildTransportTitle(type),
                    null,
                    new WindowKey(view.getPath(), type)
            );
    }

    private String buildTransportTitle(RequestTypeDto type) {
        return type.name();
    }
}
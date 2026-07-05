package com.mayak.ietms.ui.navigation;

import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.ui.workspace.request.transport.TransportRequestController;
import com.mayak.ietms.infrastructure.window.WindowKey;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.rgielen.fxweaver.core.FxWeaver;

/**
 * Facade for all in-app navigation in the desktop client.
 *
 * <p>Delegates to {@link WindowService} for modal and detached windows,
 * and to {@link HomeController} for inline (content-area) navigation.
 * Constructed per session — holds the currently logged-in user and the
 * active owner stage.
 *
 * @see NavigationType
 * @see ModalOptions
 *
 * <p>Not to be confused with {@link com.mayak.ietms.app.AppNavigator}, which
 * owns top-level screens (login/main window); this class is per-session and
 * handles navigation only within the content area of the main window.
 */

@RequiredArgsConstructor
public class ContentNavigationService {

    private final WindowService windowService;
    private final FxWeaver fxWeaver;
    private final HomeController homeController;
    private ViewLifecycle currentInlineView;
    private final UserResponseDto loggedInUser;

    @Setter
    private Stage ownerStage;

    /**
     * Returns the effective owner stage: {@code ownerStage} if set,
     * otherwise the primary stage from {@link WindowService}.
     */
    public Stage resolveOwner() {
        return ownerStage != null ? ownerStage : windowService.getPrimaryStage();
    }

    public void navigate(Class<?> controllerClass, NavigationType type) {
        navigate(controllerClass, type, null, ModalOptions.empty());
    }

    /**
     * Navigates to the given controller using the specified strategy.
     *
     * @param controllerClass the target view controller
     * @param type            navigation strategy
     * @param payload         optional data passed to the target controller;
     *                        required for {@link NavigationType#DETACHED}
     * @param options         modal configuration; ignored for non-modal types
     */
    public void navigate(Class<?> controllerClass, NavigationType type, Object payload, ModalOptions options) {
        switch (type) {
            case GLOBAL -> homeController.navigateTo(controllerClass);
            case INLINE -> openInline(controllerClass);
            case MODAL -> openModal(controllerClass, options);
            case DETACHED -> openDetached(controllerClass, payload);
        }
    }

    private void openInline(Class<?> controllerClass) {
        var loaded = fxWeaver.load(controllerClass);
        var node = (Parent) loaded.getView().orElseThrow();
        var controller = loaded.getController();

        if (currentInlineView != null) {
            currentInlineView.onHide();
            currentInlineView = null;
        }

        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);

        homeController.getContentArea().getChildren().setAll(node);
        if (controller instanceof ViewLifecycle lifecycle) {
            lifecycle.onShow();
            currentInlineView = lifecycle;
        }
    }

    private void openModal(Class<?> controllerClass, ModalOptions options) {
        Stage owner = options.ownerStage() != null ? options.ownerStage() : resolveOwner();
        windowService.openModalWindow(
                controllerClass,
                c -> {},
                options.title() != null ? options.title() : controllerClass.getSimpleName(),
                options.iconPath(),
                owner
        );
    }

    private void openDetached(Class<?> controllerClass, Object payload) {
        if (controllerClass != TransportRequestController.class) {
            throw new IllegalStateException("DETACHED navigation is allowed only for transport requests");
        }

        if (!(payload instanceof RequestTypeDto type)) {
            throw new IllegalArgumentException("Transport detached navigation requires RequestTypeDto payload");
        }

            windowService.openDetachedWindow(TransportRequestController.class,
                    controller -> {
                controller.setRequestType(type);
                controller.setHomeController(homeController);
                controller.setLoggedInUser(loggedInUser);
                },
                    type.name(),
                    null,
                    new WindowKey(TransportRequestController.class.getSimpleName(), type)
            );
    }

}

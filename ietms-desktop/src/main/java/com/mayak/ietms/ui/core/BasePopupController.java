package com.mayak.ietms.ui.core;

import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.ui.navigation.NavigationService;
import javafx.css.PseudoClass;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class for toolbar popup controllers (workspace, CRM, analytics, administration).
 *
 * <p>Provides common state — logged-in user, permissions, home controller reference,
 * and navigation service — initialized via {@link #init} after the popup is created.
 * Subclasses are instantiated once and reused across popup show/hide cycles.
 *
 * @see com.mayak.ietms.ui.home.HomeController
 */
@Getter
@Setter
public abstract class BasePopupController implements SecuredView,ViewLifecycle {

    protected HomeController homeController;
    protected Popup popup;
    protected UserResponseDto loggedInUser;
    protected UserPermissions permissions;
    protected NavigationService navigation;

    /**
     * Wires all shared dependencies into this controller.
     * Must be called once after construction, before the popup is shown for the first time.
     */
    public void init(UserResponseDto user, HomeController home, Popup popup, NavigationService navigation) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
        this.homeController = home;
        this.popup = popup;
        this.navigation = navigation;
    }

    @Override
    public void onShow() {
        if (popup != null && !popup.getContent().isEmpty()) {
            popup.getContent().getFirst()
                    .lookupAll(".popup-button")
                    .forEach(node -> node.pseudoClassStateChanged(
                            PseudoClass.getPseudoClass("hover"), false));
        }
    }
}
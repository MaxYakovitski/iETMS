package com.mayak.iet.ui.core;

import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.ui.home.HomeController;
import com.mayak.iet.ui.navigation.NavigationService;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasePopupController implements SecuredView,ViewLifecycle {

    protected HomeController homeController;
    protected Popup popup;
    protected UserResponseDto loggedInUser;
    protected UserPermissions permissions;
    protected NavigationService navigation;

    public void init(UserResponseDto user, HomeController home, Popup popup, NavigationService navigation) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
        this.homeController = home;
        this.popup = popup;
        this.navigation = navigation;
    }
}
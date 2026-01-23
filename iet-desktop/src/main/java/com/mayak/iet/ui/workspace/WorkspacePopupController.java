package com.mayak.iet.ui.workspace;

import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.ui.core.BasePopupController;
import com.mayak.iet.ui.navigation.NavigationType;
import com.mayak.iet.ui.workspace.request.transport.TransportRequestController;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class WorkspacePopupController extends BasePopupController {

    @FXML public Button plannerButton;
    @FXML public ImageView arrowIcon;

    private Popup submenuPopup;

    @Setter @Getter
    private TransportRequestController transportController;

    @Override
    public void onShow() {
        boolean showArrow =
                permissions != null
                        && permissions.canViewTransportRequests()
                        && !permissions.isAdmin();

        arrowIcon.setVisible(showArrow);
        arrowIcon.setManaged(showArrow);

        if (showArrow) {
            PopupMenuUtils.setArrowClosed(arrowIcon);
        }
    }

    @FXML
    public void handlePlanner() {
        popup.hide();
        navigation.navigate(View.PLANNER, NavigationType.GLOBAL);
        homeController.showRequestButtons(false);
    }

    @FXML
    public void handleRequests() {
        if (permissions.canViewClientRequests()) {
            popup.hide();
            navigation.navigate(View.REQUESTS_CLIENT, NavigationType.GLOBAL);
            homeController.showRequestButtons(true);
            return;
        }

        if (permissions.canViewTransportRequests()) {

            if (submenuPopup != null && submenuPopup.isShowing()) {
                submenuPopup.hide();
                return;
            }

            openSubmenu();
            return;
        }

        log.warn("User {} has no permission to view requests", loggedInUser.email());
    }

    private void openSubmenu() {
        submenuPopup = PopupMenuUtils.openPopupMenu(
                arrowIcon,
                arrowIcon,
                List.of(
                        PopupMenuUtils.menuButton(
                                "Spot",
                                () -> navigateTransport(RequestTypeDto.SPOT),
                                popup,
                                submenuPopup
                        ),
                        PopupMenuUtils.menuButton(
                                "Contract",
                                () -> navigateTransport(RequestTypeDto.CONTRACT),
                                popup,
                                submenuPopup
                        )
                )
        );
    }

    private void navigateTransport(RequestTypeDto type) {
        popup.hide();
        navigation.navigate(View.REQUESTS_TRANSPORT, NavigationType.DETACHED, type, null);
    }
}
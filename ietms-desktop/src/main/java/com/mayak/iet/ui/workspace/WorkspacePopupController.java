package com.mayak.iet.ui.workspace;

import com.mayak.iet.infrastructure.window.HoverSubmenuTracker;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.ui.core.BasePopupController;
import com.mayak.iet.ui.navigation.NavigationType;
import com.mayak.iet.ui.workspace.request.transport.TransportRequestController;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

    @FXML public HBox requestsRow, plannerRow;
    @FXML public ImageView arrowIcon;

    private Popup submenuPopup;

    @Setter @Getter
    private TransportRequestController transportController;

    @Override
    public void onShow() {
        boolean canClient = permissions.canViewClientRequests();
        boolean canTransport = permissions.canViewTransportRequests();
        boolean isAdmin = permissions.isAdmin();

        boolean showArrow = canTransport && !isAdmin;

        arrowIcon.setVisible(showArrow);
        arrowIcon.setManaged(showArrow);

        requestsRow.setOnMouseMoved(null);
        requestsRow.setOnMouseClicked(null);

        if (showArrow) {
            requestsRow.setOnMouseMoved(e -> {
                if (submenuPopup == null || !submenuPopup.isShowing()) {
                    openSubmenu();
                }
            });
        } else if (canClient) {
            requestsRow.setOnMouseClicked(e -> {
                popup.hide();
                navigation.navigate(View.REQUESTS_CLIENT, NavigationType.GLOBAL);
                homeController.showRequestButtons(true);
            });
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
        if (submenuPopup != null && submenuPopup.isShowing()) return;

        submenuPopup = PopupMenuUtils.openPopupMenu(
                requestsRow,
                List.of(
                        PopupMenuUtils.menuRow(
                                "Spot",
                                () -> navigateTransport(RequestTypeDto.SPOT),
                                popup,
                                submenuPopup
                        ),
                        PopupMenuUtils.menuRow(
                                "Contract",
                                () -> navigateTransport(RequestTypeDto.CONTRACT),
                                popup,
                                submenuPopup
                        )
                )
        );

        submenuPopup.setOnHidden(e -> submenuPopup = null);
        HoverSubmenuTracker.track(requestsRow, submenuPopup);
    }

    private void navigateTransport(RequestTypeDto type) {
        popup.hide();
        navigation.navigate(View.REQUESTS_TRANSPORT, NavigationType.DETACHED, type, null);
    }
}
package com.mayak.ietms.ui.workspace;

import com.mayak.ietms.infrastructure.window.HoverSubmenuTracker;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.ui.core.BasePopupController;
import com.mayak.ietms.ui.navigation.NavigationType;
import com.mayak.ietms.ui.workspace.planner.controller.PlannerController;
import com.mayak.ietms.ui.workspace.request.client.ClientRequestsController;
import com.mayak.ietms.ui.workspace.request.transport.TransportRequestController;
import com.mayak.ietms.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@FxmlView("workspace_popup.fxml")
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class WorkspacePopupController extends BasePopupController {

    @FXML
    public HBox requestsRow, plannerRow;

    @FXML
    public ImageView arrowIcon;

    private Popup submenuPopup;

    @Setter @Getter
    private TransportRequestController transportController;

    @Override
    public void onShow() {
        super.onShow();
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
                navigation.navigate(ClientRequestsController.class, NavigationType.GLOBAL);
                homeController.showRequestButtons(true);
            });
        }

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

    @FXML
    public void handlePlanner() {
        popup.hide();
        navigation.navigate(PlannerController.class, NavigationType.GLOBAL);
        homeController.showRequestButtons(false);
    }

    private void navigateTransport(RequestTypeDto type) {
        navigation.navigate(TransportRequestController.class, NavigationType.DETACHED, type, null);
    }
}
package com.mayak.iet.ui.analytics.controller;

import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.core.BasePopupController;
import com.mayak.iet.ui.navigation.ModalOptions;
import com.mayak.iet.ui.navigation.NavigationType;
import com.mayak.iet.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class AnalyticsPopupController extends BasePopupController {

    @FXML public Button reportButton;
    @FXML public Button statisticsButton;
    @FXML public ImageView arrowIcon;

    private Popup submenuPopup;

    @Override
    public void onShow() {
        boolean canViewAnalytics =
                permissions != null && permissions.canViewAnalytics();

        statisticsButton.setVisible(canViewAnalytics);
        statisticsButton.setManaged(canViewAnalytics);

        arrowIcon.setVisible(canViewAnalytics);
        arrowIcon.setManaged(canViewAnalytics);

        if (canViewAnalytics) {
            PopupMenuUtils.setArrowClosed(arrowIcon);
        }
    }

    @FXML
    public void handleReport() {
        popup.hide();
        navigation.navigate(
                View.STATISTICS_REPORT,
                NavigationType.MODAL,
                null,
                new ModalOptions("Statistics report", "/icons/graph.png"));
    }

    @FXML
    public void handleStatistics() {
        if (submenuPopup != null && submenuPopup.isShowing()) {
            submenuPopup.hide();
            return;
        }
        openSubmenu();
    }

    private void openSubmenu() {
        submenuPopup = PopupMenuUtils.openPopupMenu(
                arrowIcon,
                arrowIcon,
                List.of(
                        PopupMenuUtils.menuButton(
                                "Departments",
                                () -> navigate(View.STATISTICS_DEPARTMENT),
                                popup,
                                submenuPopup
                        ),
                        PopupMenuUtils.menuButton(
                                "Employees",
                                () -> navigate(View.STATISTICS_EMPLOYEES),
                                popup,
                                submenuPopup
                        ),
                        PopupMenuUtils.menuButton(
                                "Companies",
                                () -> navigate(View.STATISTICS_COMPANIES),
                                popup,
                                submenuPopup
                        )
                )
        );
    }

    private void navigate(View view) {
        popup.hide();
        navigation.navigate(view, NavigationType.GLOBAL);
    }
}
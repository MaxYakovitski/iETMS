package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.infrastructure.window.HoverSubmenuTracker;
import com.mayak.ietms.ui.core.BasePopupController;
import com.mayak.ietms.ui.navigation.ModalOptions;
import com.mayak.ietms.ui.navigation.NavigationType;
import com.mayak.ietms.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller for the analytics popup menu.
 * Provides navigation to statistics views (departments, employees, companies)
 * and the statistics report modal.
 */
@Controller
@FxmlView("analytics_popup.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsPopupController extends BasePopupController {

    @FXML
    public HBox statisticsRow;

    private Popup submenuPopup;

    @Override
    public void onShow() {
        super.onShow();
        statisticsRow.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (submenuPopup == null || !submenuPopup.isShowing()) {
                openStatisticsSubmenu();
            }
        });
    }

    @FXML
    public void handleReport() {
        popup.hide();
        navigation.navigate(
                ReportController.class,
                NavigationType.MODAL,
                null,
                new ModalOptions("Statistics reports", "/icons/bar-chart.png", navigation.resolveOwner()));
    }

    private void openStatisticsSubmenu() {
        if (submenuPopup != null && submenuPopup.isShowing()) return;

        submenuPopup = PopupMenuUtils.openPopupMenu(
                statisticsRow,
                List.of(
                        PopupMenuUtils.menuRow(
                                "Departments",
                                () -> navigation.navigate(DepartmentStatisticsController.class, NavigationType.GLOBAL),
                                popup,
                                submenuPopup
                        ),
                        PopupMenuUtils.menuRow(
                                "Employees",
                                () -> navigation.navigate(UserStatisticsController.class, NavigationType.GLOBAL),
                                popup,
                                submenuPopup
                        ),
                        PopupMenuUtils.menuRow(
                                "Companies",
                                () -> navigation.navigate(CompanyStatisticsController.class, NavigationType.GLOBAL),
                                popup,
                                submenuPopup
                        )
                )
        );
        submenuPopup.setOnHidden(e -> submenuPopup = null);
        HoverSubmenuTracker.track(statisticsRow, submenuPopup);
    }
}
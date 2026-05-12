package com.mayak.ietms.ui.administration;

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
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Controller for the administration popup menu.
 * Provides navigation to structure settings (departments, employees),
 * dictionary settings (locations), and the license management screen.
 */
@Controller
@FxmlView("administration_popup.fxml")
@Scope("prototype")
@RequiredArgsConstructor
public class AdministrationPopupController extends BasePopupController {

    @FXML
    public HBox structureRow, dictionariesRow;

    private Popup structurePopup, dictionariesPopup;

    @Override
    public void onShow() {
        super.onShow();
        structureRow.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (structurePopup == null || !structurePopup.isShowing()) {
                openStructureSubmenu();
            }
        });

        dictionariesRow.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (dictionariesPopup == null || !dictionariesPopup.isShowing()) {
                openDictionariesSubmenu();
            }
        });
    }

    private void openStructureSubmenu() {
        if (structurePopup != null && structurePopup.isShowing()) {
            return;
        }

        structurePopup = PopupMenuUtils.openPopupMenu(
                structureRow,
                List.of(
                        PopupMenuUtils.menuRow(
                                "Departments",
                                () -> navigation.navigate(DepartmentSettingsController.class, NavigationType.GLOBAL),
                                popup,
                                structurePopup),
                        PopupMenuUtils.menuRow(
                                "Employees",
                                () -> navigation.navigate(UserSettingsController.class, NavigationType.GLOBAL),
                                popup,
                                structurePopup)
                )
        );
        structurePopup.setOnHidden(e -> structurePopup = null);
        HoverSubmenuTracker.track(structureRow, structurePopup);
    }

    private void openDictionariesSubmenu() {
        if (dictionariesPopup != null && dictionariesPopup.isShowing()) {
            return;
        }

        dictionariesPopup = PopupMenuUtils.openPopupMenu(
                dictionariesRow,
                List.of(
                        PopupMenuUtils.menuRow(
                                "Locations",
                                () -> navigation.navigate(LocationSettingsController.class, NavigationType.GLOBAL),
                                popup,
                                dictionariesPopup)
                )
        );

        dictionariesPopup.setOnHidden(e -> dictionariesPopup = null);
        HoverSubmenuTracker.track(dictionariesRow, dictionariesPopup);
    }

    @FXML
    public void handleLicense() {
        popup.hide();
        navigation.navigate(
                LicenseSettingsController.class,
                NavigationType.MODAL,
                null,
                new ModalOptions("License", "/icons/license.png", navigation.resolveOwner()));
    }
}
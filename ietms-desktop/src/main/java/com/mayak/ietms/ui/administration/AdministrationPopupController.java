package com.mayak.ietms.ui.administration;

import com.mayak.ietms.infrastructure.window.HoverSubmenuTracker;
import com.mayak.ietms.ui.core.BasePopupController;
import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.navigation.NavigationType;
import com.mayak.ietms.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
public class AdministrationPopupController extends BasePopupController {

    @FXML public HBox structureRow, dictionariesRow;

    private Popup structurePopup;
    private Popup dictionariesPopup;

    @Override
    public void onShow() {

        structureRow.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (structurePopup == null || !structurePopup.isShowing()) {
                handleStructure();
            }
        });

        dictionariesRow.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (dictionariesPopup == null || !dictionariesPopup.isShowing()) {
                handleDictionaries();
            }
        });
    }

    @FXML
    public void handleStructure() {
        if (structurePopup != null && structurePopup.isShowing()) {
            structurePopup.hide();
            return;
        }

        structurePopup = PopupMenuUtils.openPopupMenu(
                structureRow,
                List.of(
                        PopupMenuUtils.menuRow(
                                "Departments",
                                () -> navigate(View.SETTINGS_DEPARTMENTS),
                                popup,
                                structurePopup
                        ),
                        PopupMenuUtils.menuRow(
                                "Employees",
                                () -> navigate(View.SETTINGS_USERS),
                                popup,
                                structurePopup
                        )
                )
        );

        structurePopup.setOnHidden(e -> structurePopup = null);
        HoverSubmenuTracker.track(structureRow, structurePopup);
    }

    @FXML
    public void handleDictionaries() {
        if (dictionariesPopup != null && dictionariesPopup.isShowing()) {
            dictionariesPopup.hide();
            return;
        }

        dictionariesPopup = PopupMenuUtils.openPopupMenu(
                dictionariesRow,
                List.of(
                        PopupMenuUtils.menuRow(
                                "Locations",
                                () -> navigate(View.SETTINGS_LOCATION),
                                popup,
                                dictionariesPopup
                        )
                )
        );

        dictionariesPopup.setOnHidden(e -> dictionariesPopup = null);
        HoverSubmenuTracker.track(dictionariesRow, dictionariesPopup);
    }

    private void navigate(View view) {
        navigation.navigate(view, NavigationType.GLOBAL);
    }
}
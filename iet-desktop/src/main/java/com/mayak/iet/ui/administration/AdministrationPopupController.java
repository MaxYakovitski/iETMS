package com.mayak.iet.ui.administration;

import com.mayak.iet.ui.core.BasePopupController;
import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.navigation.NavigationType;
import com.mayak.iet.infrastructure.window.PopupMenuUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
public class AdministrationPopupController extends BasePopupController {

    @FXML public ImageView arrowStructureIcon;
    @FXML public ImageView arrowDictionariesIcon;

    private Popup structurePopup;
    private Popup dictionariesPopup;

    @Override
    public void onShow() {
        PopupMenuUtils.setArrowClosed(arrowStructureIcon);
        PopupMenuUtils.setArrowClosed(arrowDictionariesIcon);
    }

    @FXML
    public void handleStructure() {
        if (structurePopup != null && structurePopup.isShowing()) {
            structurePopup.hide();
            return;
        }

        structurePopup = PopupMenuUtils.openPopupMenu(
                arrowStructureIcon,
                arrowStructureIcon,
                List.of(
                        PopupMenuUtils.menuButton(
                                "Departments",
                                () -> navigate(View.SETTINGS_DEPARTMENTS),
                                popup,
                                structurePopup
                        ),
                        PopupMenuUtils.menuButton(
                                "Users",
                                () -> navigate(View.SETTINGS_USERS),
                                popup,
                                structurePopup
                        )
                )
        );
    }

    @FXML
    public void handleDictionaries() {
        if (dictionariesPopup != null && dictionariesPopup.isShowing()) {
            dictionariesPopup.hide();
            return;
        }

        dictionariesPopup = PopupMenuUtils.openPopupMenu(
                arrowDictionariesIcon,
                arrowDictionariesIcon,
                List.of(
                        PopupMenuUtils.menuButton(
                                "Locations",
                                () -> navigate(View.SETTINGS_LOCATION),
                                popup,
                                dictionariesPopup
                        )
                )
        );
    }

    private void navigate(View view) {
        navigation.navigate(view, NavigationType.INLINE);
    }
}
package com.mayak.ietms.ui.crm;

import com.mayak.ietms.ui.core.BasePopupController;
import com.mayak.ietms.ui.navigation.NavigationType;
import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the CRM toolbar popup menu.
 * Navigates to the companies or contracts view.
 */
@Controller
@FxmlView("crm_popup.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class CrmPopupController extends BasePopupController {

    @FXML
    public void handleCompanies() {
        popup.hide();
        navigation.navigate(CompanyController.class, NavigationType.GLOBAL);
    }

    @FXML
    public void handleContracts() {
        popup.hide();
        navigation.navigate(ContractsController.class, NavigationType.GLOBAL);
    }
}
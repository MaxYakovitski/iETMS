package com.mayak.ietms.ui.crm;

import com.mayak.ietms.support.enums.View;
import com.mayak.ietms.ui.core.BasePopupController;
import com.mayak.ietms.ui.navigation.NavigationType;
import javafx.fxml.FXML;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class CrmPopupController extends BasePopupController {

    @FXML
    public void handleCompanies() {
        popup.hide();
        navigation.navigate(View.CRM_COMPANIES, NavigationType.GLOBAL);
    }

    @FXML
    public void handleContracts() {
        popup.hide();
        navigation.navigate(View.CRM_CONTRACTS, NavigationType.GLOBAL);
    }
}
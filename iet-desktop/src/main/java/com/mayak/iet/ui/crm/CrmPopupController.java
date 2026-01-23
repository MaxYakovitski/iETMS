package com.mayak.iet.ui.crm;

import com.mayak.iet.support.enums.View;
import com.mayak.iet.ui.core.BasePopupController;
import com.mayak.iet.ui.navigation.NavigationType;
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
        navigation.navigate(View.CRM_COMPANIES, NavigationType.INLINE);
    }

    @FXML
    public void handleContracts() {
        popup.hide();
        navigation.navigate(View.CRM_CONTRACTS, NavigationType.INLINE);
    }
}
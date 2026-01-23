package com.mayak.iet.ui.workspace.request.transport;

import com.mayak.iet.integration.api.RequestClient;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.integration.websocket.RequestStompClient;
import com.mayak.iet.ui.workspace.request.base.AbstractRequestController;
import com.mayak.iet.ui.workspace.request.base.ParentType;
import com.mayak.iet.support.state.RequestFilterState;
import com.mayak.iet.infrastructure.window.WindowService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
@Slf4j
public class TransportRequestController extends AbstractRequestController {

    @FXML public Button filterActiveButton;

    public void setRequestType(RequestTypeDto type) {
        this.requestType = type;
    }

    @Override
    public ParentType getParentType() {
        return ParentType.TRANSPORT;
    }

    public TransportRequestController(
            RequestClient requestClient,
            WindowService windowService,
            RequestFilterState filterStateService,
            RequestStompClient wsClient) {
        super(requestClient, windowService, filterStateService,  wsClient);
    }

    @FXML
    public void initialize() {
        filterActiveButton.setOnAction(event -> onFilterHotkey());
    }

    @Override
    public void onShow() {
        if (requestType == null) {
            return;
        }

        filterState.clear();
        super.onShow();
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.focusedProperty().addListener((foc, oldFoc, newFoc) -> {
            if (newFoc && getHomeController() != null) {
                getHomeController().setRequestsParent(this);
                log.info("Active RequestsParent switched to TRANSPORT ({})", requestType);
            }
        });
    }

    public void setFilterActive(boolean active) {
        filterActiveButton.setVisible(active);
        filterActiveButton.setManaged(active);
    }

    @Override
    protected boolean allowDuplicateHotkey() {
        return true;
    }

    @Override
    protected boolean supportsFilterHotkey() { return true; }

    @Override
    protected boolean supportsSearchHotkey() { return true; }

    @Override
    protected void onFilterHotkey() {
        if (getHomeController() != null) {
            getHomeController().handleFilter();
        }
    }

    @Override
    protected void onSearchHotkey() {
        if (getHomeController() != null) {
            getHomeController().handleSearchHotkey();
        }
    }

    @Override
    protected void onPageLoaded() {
        boolean active = filterState.get().isPresent();
        setFilterActive(active);
    }
}
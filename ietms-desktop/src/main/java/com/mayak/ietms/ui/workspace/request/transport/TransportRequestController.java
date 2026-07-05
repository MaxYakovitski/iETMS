package com.mayak.ietms.ui.workspace.request.transport;

import com.mayak.ietms.integration.api.RequestClient;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.integration.websocket.RequestStompClient;
import com.mayak.ietms.ui.core.RequiresPermission;
import com.mayak.ietms.ui.core.ViewPermission;
import com.mayak.ietms.ui.navigation.ContentNavigationService;
import com.mayak.ietms.ui.workspace.request.base.AbstractRequestController;
import com.mayak.ietms.support.state.RequestFilterState;
import com.mayak.ietms.infrastructure.window.WindowService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Objects;

/**
 * Controller for the transport requests workspace.
 *
 * <p>Opens in a detached (non-modal) window, one instance per
 * {@link com.mayak.ietms.request.dto.enums.RequestTypeDto}.
 * The {@code requestType} must be injected before the view is shown —
 * see {@link ContentNavigationService}.
 *
 * <p>Declared {@code prototype}-scoped because multiple instances
 * (one per type) can be open simultaneously.
 */
@Controller
@FxmlView("requests_transport.fxml")
@Scope("prototype")
@RequiresPermission(ViewPermission.TRANSPORT_REQUESTS)
@Slf4j
public class TransportRequestController extends AbstractRequestController {

    // ==================== Constants ====================
    private static final String FILTER_ICON_DEFAULT = "/icons/filter-default.png";
    private static final String FILTER_ICON_ACTIVE  = "/icons/filter-active.png";

    // ==================== FXML ====================
    @FXML
    private Button filterButton;

    @FXML
    private ImageView filterImageView;

    @FXML
    private TextField searchField;

    // ==================== Fields ====================
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));

    // ==================== Constructor ====================
    public TransportRequestController(
            RequestClient requestClient,
            WindowService windowService,
            FxWeaver fxWeaver,
            RequestFilterState filterStateService,
            RequestStompClient wsClient) {
        super(requestClient, windowService, fxWeaver, filterStateService,  wsClient);
    }

    // ==================== FXML Lifecycle ====================

    @FXML
    public void initialize() {
        searchDebounce.setOnFinished(e -> {
            String value = searchField.getText();
            String query = (value == null || value.isBlank()) ? null : value.trim();
            applySearch(query);
        });
        searchField.textProperty().addListener((obs, o, n) -> searchDebounce.playFromStart());
    }

    // ==================== Lifecycle ====================

    /**
     * Guards against premature calls — requestType is set externally after construction.
     */
    @Override
    public void onShow() {
        if (requestType == null) return;
        filterState.clear();
        super.onShow();
    }

    /**
     * Transport opens in a separate window. Re-registers the active RequestsParent
     * on focus because multiple transport windows can be open simultaneously.
     */
    @Override
    public void setStage(Stage stage) {
        super.setStage(stage);
        stage.focusedProperty().addListener((foc, oldFoc, newFoc) -> {
            if (newFoc && getHomeController() != null) {
                getHomeController().setRequestsParent(this);
                log.info("Active RequestsParent switched to TRANSPORT ({})", requestType);
            }
        });
    }

    // ==================== Public API ====================

    public void setRequestType(RequestTypeDto type) {
        this.requestType = type;
    }

    public void setFilterActive(boolean active) {
        String path = active ? FILTER_ICON_ACTIVE : FILTER_ICON_DEFAULT;
        filterImageView.setImage(new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm()
        ));
        filterButton.setOpacity(active ? 1.0 : 0.65);
    }

    // ==================== FXML Handlers ====================

    @FXML
    public void handleFilter() {
        onFilterHotkey();
    }

    // ==================== Overrides ====================

    @Override
    protected boolean allowDuplicateHotkey() {
        return true;
    }

    @Override
    protected boolean supportsFilterHotkey() {
        return true;
    }

    @Override
    protected void onPageLoaded() {
        boolean active = filterState.get().isPresent();
        setFilterActive(active);
    }
}
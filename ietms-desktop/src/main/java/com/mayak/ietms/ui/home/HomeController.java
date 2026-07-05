package com.mayak.ietms.ui.home;

import com.mayak.ietms.infrastructure.window.PopupMenuUtils;
import com.mayak.ietms.integration.api.UserClient;
import com.mayak.ietms.ui.about.AboutController;
import com.mayak.ietms.ui.core.*;
import com.mayak.ietms.ui.dashboard.DashboardController;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.analytics.controller.AnalyticsPopupController;
import com.mayak.ietms.ui.crm.CrmPopupController;
import com.mayak.ietms.ui.navigation.ContentNavigationService;
import com.mayak.ietms.ui.user.UserController;
import com.mayak.ietms.ui.workspace.request.filter.RequestFilterController;
import com.mayak.ietms.ui.workspace.request.base.RequestsParent;
import com.mayak.ietms.ui.workspace.WorkspacePopupController;
import com.mayak.ietms.ui.administration.AdministrationPopupController;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.Optional;

/**
 * Main application controller. Manages the primary content area, navigation,
 * toolbar actions, and popup menus after successful login.
 *
 * <p>Declared {@code prototype}-scoped because a fresh instance is created
 * on each login (the Spring context is re-created on logout).
 */
@Controller
@FxmlView("home.fxml")
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    // ==================== Constants ====================
    private static final String USER_ICON = "/icons/user.png";
    private static final String FILTER_ICON_DEFAULT = "/icons/filter-default.png";
    private static final String FILTER_ICON_ACTIVE = "/icons/filter-active.png";
    private static final String ABOUT_ICON = "/icons/info.png";

    // ==================== FXML ====================
    @FXML
    @Getter
    private AnchorPane contentArea;

    @FXML
    private Button filterButton;

    @FXML
    private TextField searchField;

    @FXML
    private ImageView filterImageView;

    @FXML
    private Button analyticsButton, administrationButton;

    // ==================== Dependencies ====================
    private final UserClient userClient;
    private final WindowService windowService;
    private final FxWeaver fxWeaver;
    @Getter
    private ContentNavigationService navigation;

    // ==================== State ====================
    @Setter
    private RequestsParent requestsParent;
    private ViewLifecycle currentViewController;
    private UserResponseDto loggedInUser;
    private UserPermissions permissions;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));

    // ==================== Popups ====================
    private Popup workspacePopup;
    private Popup crmPopup;
    private Popup administrationPopup;
    private Popup analyticsPopup;

    // ==================== FXML Lifecycle ====================

    @FXML
    public void initialize() {
        try {
            this.loggedInUser = userClient.getMe();
            this.permissions = new UserPermissions(loggedInUser);
            this.navigation = new ContentNavigationService(windowService, fxWeaver,this, loggedInUser);
            applyUserPermissions();
            navigateTo(DashboardController.class);
        } catch (Exception e) {
            log.error("Failed to load current user", e);
        }

        searchDebounce.setOnFinished(e -> {if (requestsParent != null) handleSearch(searchField.getText());});
        searchField.textProperty().addListener((obs, o, n) -> searchDebounce.playFromStart());
    }

    // ==================== Navigation ====================

    /**
     * Loads a view into the content area, managing the full lifecycle:
     * hides the current view, wires up SecuredView / ViewLifecycle / RequestsParent,
     * and toggles the toolbar request buttons.
     *
     * <p>If the user lacks permission for {@code controllerClass}
     * (as declared by {@link RequiresPermission}), silently redirects to the dashboard.
     */
    public void navigateTo(Class<?> controllerClass) {
        if (loggedInUser != null && !canOpenView(controllerClass)) {
            log.warn("User {} attempted to open {} without permission", loggedInUser.email(), controllerClass.getSimpleName());
            controllerClass = DashboardController.class;
        }

        if (currentViewController != null) {
            currentViewController.onHide();
        }

        var loaded = fxWeaver.load(controllerClass);
        Node node = loaded.getView().orElseThrow();
        Object controller = loaded.getController();

        contentArea.getChildren().setAll(node);

        AnchorPane.setTopAnchor(node, 0.0);
        AnchorPane.setRightAnchor(node, 0.0);
        AnchorPane.setBottomAnchor(node, 0.0);
        AnchorPane.setLeftAnchor(node, 0.0);

        currentViewController = null;
        requestsParent = null;

        if (controller instanceof SecuredView secured) {
            secured.setLoggedInUser(loggedInUser);
        }

        if (controller instanceof ViewLifecycle lifecycle) {
            lifecycle.onShow();
            currentViewController = lifecycle;
        }

        if (controller instanceof RequestsParent rp) {
            this.requestsParent = rp;
            rp.setHomeController(this);
        }

        showRequestButtons(controller instanceof RequestsParent);
    }

    // no annotation → unrestricted; annotation present → check permissions
    private boolean canOpenView(Class<?> controllerClass) {
        var annotation = controllerClass.getAnnotation(RequiresPermission.class);
        return annotation == null || permissions.hasPermission(annotation.value());
    }

    // ==================== FXML Handlers ====================

    @FXML
    public void handleHome() {
        navigateTo(DashboardController.class);
    }

    @FXML
    public void handleWorkspace(ActionEvent actionEvent) {
        workspacePopup = createPopupIfAbsent(workspacePopup, WorkspacePopupController.class);
        PopupMenuUtils.togglePopup(workspacePopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleCRM(ActionEvent actionEvent) {
        if (!permissions.hasPermission(ViewPermission.CRM)) {
            AlertUtils.showWarning("Access denied!");
            return;
        }
        crmPopup = createPopupIfAbsent(crmPopup, CrmPopupController.class);
        PopupMenuUtils.togglePopup(crmPopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleAnalytics(ActionEvent actionEvent) {
        analyticsPopup = createPopupIfAbsent(analyticsPopup, AnalyticsPopupController.class);
        PopupMenuUtils.togglePopup(analyticsPopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleAdministration(ActionEvent actionEvent) {
        administrationPopup = createPopupIfAbsent(administrationPopup, AdministrationPopupController.class);
        PopupMenuUtils.togglePopup(administrationPopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleUser() {
        windowService.openModalWindow(
                UserController.class,
                controller -> controller.setUser(loggedInUser),
                "User",
                USER_ICON
        );
    }

    @FXML
    public void handleAbout() {
        windowService.openModalWindow(
                AboutController.class,
                c -> {},
                "About",
                ABOUT_ICON
        );
    }

    @FXML
    public void handleFilter() {
        handleFilter(null);
    }

    // ==================== Public API ====================

    /** Opens the filter modal anchored to the given owner stage (or primary if null). */
    public void handleFilter(Stage owner) {
        windowService.openModalWindow(RequestFilterController.class,
                controller ->  {
            controller.setLoggedInUser(loggedInUser);
            if (requestsParent != null) {
                controller.setRequestsParent(requestsParent);
            }
            controller.onShow();
            },
                "Filter",
                FILTER_ICON_DEFAULT,
                owner
        );
    }

    /** Updates the filter button icon to reflect whether a filter is currently active. */
    public void updateFilterState(boolean active) {
        setFilterButtonActive(active);
    }

    /** Shows or hides the filter and search toolbar controls. */
    public void showRequestButtons(boolean show) {
        filterButton.setVisible(show);
        filterButton.setManaged(show);
        searchField.setVisible(show);
        searchField.setManaged(show);
    }

    // ==================== Private Helpers ====================

    private void handleSearch(String query) {
        Optional.ofNullable(requestsParent).ifPresent(rp -> rp.applySearch(query));
    }

    // popups are created once and reused; controller is wired via BasePopupController.init
    private <T> Popup createPopupIfAbsent(Popup popup, Class<T> controllerClass) {
        if (popup != null) return popup;
        popup = new Popup();

        var loaded = fxWeaver.load(controllerClass);
        T controller = loaded.getController();
        Node node = loaded.getView().orElseThrow();
        node.setUserData(controller);

        if (controller instanceof BasePopupController base) {
            base.init(loggedInUser, this, popup, navigation);
        }

        popup.getContent().add(node);
        popup.setAutoHide(true);
        return popup;
    }

    private void setFilterButtonActive(boolean active) {
        if (active) {
            filterImageView.setImage(new Image(
                    Objects.requireNonNull(getClass().getResource(FILTER_ICON_ACTIVE))
                            .toExternalForm()));
        } else {
            filterImageView.setImage(new Image(
                    Objects.requireNonNull(getClass().getResource(FILTER_ICON_DEFAULT))
                            .toExternalForm()));
        }
    }

    private void applyUserPermissions() {
        boolean admin = permissions != null && permissions.isAdmin();
        administrationButton.setVisible(admin);
        administrationButton.setManaged(admin);

        boolean canAnalytics = permissions != null && permissions.canViewAnalytics();
        analyticsButton.setVisible(canAnalytics);
        analyticsButton.setManaged(canAnalytics);
    }
}
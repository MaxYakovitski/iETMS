package com.mayak.ietms.ui.home;

import com.mayak.ietms.infrastructure.window.PopupMenuUtils;
import com.mayak.ietms.integration.api.UserClient;
import com.mayak.ietms.ui.about.AboutController;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.analytics.controller.AnalyticsPopupController;
import com.mayak.ietms.ui.core.BasePopupController;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.UserPermissions;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.crm.CrmPopupController;
import com.mayak.ietms.ui.navigation.NavigationService;
import com.mayak.ietms.ui.user.UserController;
import com.mayak.ietms.ui.workspace.request.filter.RequestFilterController;
import com.mayak.ietms.ui.workspace.request.base.RequestsParent;
import com.mayak.ietms.ui.workspace.WorkspacePopupController;
import com.mayak.ietms.ui.administration.AdministrationPopupController;
import com.mayak.ietms.ui.workspace.request.search.SearchController;
import com.mayak.ietms.ui.workspace.request.transport.TransportRequestController;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.support.enums.View;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.Objects;
import java.util.Optional;

@Controller
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    @FXML
    @Getter private AnchorPane contentArea;
    @FXML public Button filterButton;
    @FXML public TextField searchField;
    @FXML public ImageView filterImageView;
    @FXML public Button administrationButton;

    private final UserClient userClient;
    private final WindowService windowService;
    @Getter private NavigationService navigation;

    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(300));

    @Setter private RequestsParent requestsParent;
    private ViewLifecycle currentViewController;
    private UserResponseDto loggedInUser;
    private UserPermissions permissions;
    private Stage searchWindow;

    private Popup workspacePopup;
    private Popup crmPopup;
    private Popup administrationPopup;
    private Popup analyticsPopup;

    private static final String USER_ICON = "/icons/user.png";
    private static final String FILTER_ICON_DEFAULT = "/icons/filter-default.png";
    private static final String FILTER_ICON_ACTIVE = "/icons/filter-active.png";
    private static final String SEARCH_ICON = "/icons/search.png";
    private static final String ABOUT_ICON = "/icons/info.png";

    @FXML
    public void initialize() {
        try {
            this.loggedInUser = userClient.getMe();
            this.permissions = new UserPermissions(loggedInUser);
            this.navigation = new NavigationService(windowService, this, loggedInUser);
            applyUserPermissions();
            loadView(View.DASHBOARD);
        } catch (Exception e) {
            log.error("Failed to load current user", e);
        }

        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchDebounce.setOnFinished(event -> {
                if (requestsParent != null) {
                    handleSearch(newText);
                }
            });
            searchDebounce.playFromStart();
        });
    }

    @FXML
    public void handleHome() {
        showRequestButtons(false);
        loadView(View.DASHBOARD);
    }

    @FXML
    public void handleWorkspace(ActionEvent actionEvent) {
        workspacePopup = createPopupIfAbsent(workspacePopup, View.WORKSPACE_POPUP, WorkspacePopupController.class);
        PopupMenuUtils.togglePopup(workspacePopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleCRM(ActionEvent actionEvent) {
        if (!permissions.canViewCrm()) {
            AlertUtils.showWarning("Access denied!");
            return;
        }

        crmPopup = createPopupIfAbsent(
                crmPopup,
                View.CRM_POPUP,
                CrmPopupController.class
        );
        PopupMenuUtils.togglePopup(crmPopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleAnalytics(ActionEvent actionEvent) {
        if (!permissions.canViewAnalytics()) {
            AlertUtils.showWarning("Access denied!");
            return;
        }

        analyticsPopup = createPopupIfAbsent(
                analyticsPopup,
                View.ANALYTICS_POPUP,
                AnalyticsPopupController.class
        );
        PopupMenuUtils.togglePopup(analyticsPopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleAdministration(ActionEvent actionEvent) {
        if (!permissions.canViewAdministration()) {
            AlertUtils.showWarning("Access denied!");
            return;
        }

        administrationPopup = createPopupIfAbsent(
                administrationPopup,
                View.ADMINISTRATION_POPUP,
                AdministrationPopupController.class
        );
        PopupMenuUtils.togglePopup(administrationPopup, (Node) actionEvent.getSource());
    }

    @FXML
    public void handleUser() {
        windowService.openModalWindow(
                View.USER.getPath(),
                UserController.class,
                controller -> controller.init(windowService.getPrimaryStage(), loggedInUser),
                "User",
                USER_ICON
        );
    }

    @FXML
    public void handleAbout() {
        windowService.openModalWindow(
                View.ABOUT.getPath(),
                AboutController.class,
                controller -> controller.init(windowService.getPrimaryStage()),
                "About",
                ABOUT_ICON
        );
    }

    @FXML
    public void handleFilter() {
        windowService.openModalWindow(
                View.FILTER.getPath(),
                RequestFilterController.class,
                controller ->  {
                    controller.setLoggedInUser(loggedInUser);
                    if (requestsParent != null) {
                        controller.setRequestsParent(requestsParent);
                    }
                    controller.onShow();
                },
                "Filter",
                FILTER_ICON_DEFAULT
        );
    }

    public void handleSearchHotkey() {
        if (requestsParent instanceof TransportRequestController trc) {
            openSearchWindow(trc);
            return;
        }
        searchField.requestFocus();
    }

    private void openSearchWindow(TransportRequestController trc) {
        if (searchWindow != null && searchWindow.isShowing()) {
            searchWindow.requestFocus();
            return;
        }

        windowService.openModalWindow(
                View.SEARCH.getPath(),
                SearchController.class,
                controller -> {
                    controller.init(trc);
                    this.searchWindow = controller.getStage();
                },
                "Search",
                SEARCH_ICON
        );
    }

    private void handleSearch(String query) {
        Optional.ofNullable(requestsParent).ifPresent(rp -> rp.applySearch(query));
    }

    public void loadView(View view) {
        if (loggedInUser != null && !canOpenView(view)) {
            log.warn("User {} attempted to open {} without permission", loggedInUser.email(), view);
            view = View.DASHBOARD;
        }

        if (currentViewController != null) {
            currentViewController.onHide();
        }
        WindowService.Loaded<?> loaded = windowService.loadControllerWithNode(view.getPath());
        Node node = loaded.node();
        Object controller = loaded.controller();

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

        showRequestButtons(view == View.REQUESTS_CLIENT || view == View.REQUESTS_TRANSPORT);
    }

    private boolean canOpenView(View view) {
        return switch (view) {

            case REQUESTS_CLIENT ->
                    permissions.canViewClientRequests();

            case REQUESTS_TRANSPORT ->
                    permissions.canViewTransportRequests();

            case CRM_POPUP ->
                    permissions.canViewCrm();

            case ANALYTICS_POPUP ->
                    permissions.canViewAnalytics();

            case ADMINISTRATION_POPUP ->
                    permissions.canViewAdministration();

            default -> true;
        };
    }

    private <T> Popup createPopupIfAbsent(Popup popup, View view, Class<T> controllerClass) {
        if (popup != null) return popup;
        popup = new Popup();

        var loaded = windowService.loadControllerWithNode(view.getPath(), controllerClass);
        Object controller = loaded.controller();
        Node node = loaded.node();
        node.setUserData(controller);

        if (controller instanceof BasePopupController base) {
            base.init(loggedInUser, this, popup, navigation);
        }

        popup.getContent().add(node);
        popup.setAutoHide(true);

        return popup;
    }

    public void updateFilterState(boolean active) {
        setFilterButtonActive(active);
    }

    public void showRequestButtons(boolean show) {
        filterButton.setVisible(show);
        filterButton.setManaged(show);
        searchField.setVisible(show);
        searchField.setManaged(show);
    }

    private void setFilterButtonActive(boolean active) {
        if (active) {
            filterImageView.setImage(new Image(Objects.requireNonNull(getClass().getResource(FILTER_ICON_ACTIVE)).toExternalForm()));
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
    }
}
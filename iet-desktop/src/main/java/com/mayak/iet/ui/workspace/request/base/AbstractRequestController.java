package com.mayak.iet.ui.workspace.request.base;

import com.mayak.iet.request.dto.enums.RequestStatusDto;
import com.mayak.iet.request.dto.enums.RequestTypeDto;
import com.mayak.iet.request.dto.event.RequestEventDto;
import com.mayak.iet.infrastructure.tost.ToastService;
import com.mayak.iet.integration.api.RequestClient;
import com.mayak.iet.request.dto.filter.RequestFilterDto;
import com.mayak.iet.request.dto.view.RequestDetailsDto;
import com.mayak.iet.request.dto.view.RequestListItemDto;
import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.request.event.RequestEvent;
import com.mayak.iet.ui.core.SecuredView;
import com.mayak.iet.ui.core.ViewLifecycle;
import com.mayak.iet.ui.home.HomeController;
import com.mayak.iet.support.state.RequestFilterState;
import com.mayak.iet.infrastructure.error.AlertUtils;
import com.mayak.iet.infrastructure.window.WindowService;
import com.mayak.iet.integration.websocket.RequestStompClient;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRequestController implements ViewLifecycle, SecuredView, RequestsParent {

    @FXML protected ListView<RequestListItemDto> requestsListView;
    @FXML protected StackPane loadingOverlay;
    @FXML protected ProgressIndicator loadingIndicator;
    @FXML protected Label emptyMessageLabel;

    @Getter @Setter
    HomeController homeController;

    @Getter
    private UserResponseDto loggedInUser;

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
    }

    protected final RequestClient requestClient;
    protected final WindowService windowService;
    protected final RequestFilterState filterState;

    @Getter
    protected final ObservableList<RequestListItemDto> requestItems = FXCollections.observableArrayList();

    protected RequestFilterDto activeFilter;

    protected String activeSearchQuery;
    @Getter @Setter
    protected RequestTypeDto requestType;
    @Getter @Setter
    protected Stage stage;

    protected static final int PAGE_SIZE = 100;
    protected int currentPage = 0;
    protected boolean loading = false;
    protected boolean allLoaded = false;
    private boolean overlayVisible = false;
    private boolean hotkeysRegistered = false;

    protected final RequestStompClient wsClient;
    protected ScheduledExecutorService uiUpdater;

    protected volatile boolean active = true;
    protected volatile boolean pauseUpdates = false;

    @Override
    public void setupListView() {
        requestsListView.setItems(requestItems);
        requestsListView.setCellFactory(cell -> new RequestCell(windowService, loggedInUser, this));

        requestsListView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            ScrollBar verticalBar = getVerticalScrollBar(requestsListView);
            if (verticalBar != null) {
                verticalBar.valueProperty().addListener((val, oldVal, newVal) -> {
                    if (!allLoaded && !loading && newVal.doubleValue() >= verticalBar.getMax()) {
                        loadNextPage();
                    }
                });
            }
        });

        requestsListView.setOnKeyPressed(event -> {
            if (!allowDuplicateHotkey()) return;

            boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
            boolean shortcut = (isMac && event.isMetaDown()) || (!isMac && event.isControlDown());

            if (shortcut && event.getCode() == KeyCode.D) {
                RequestListItemDto selected = requestsListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadDetailsAndFillForm(selected.id());
                    event.consume();
                }
            }
        });
    }

    protected void loadDetailsAndFillForm(Long requestId) {
        try {
            RequestDetailsDto details = requestClient.getDetails(requestId);
            fillFormWithRequest(details);
        } catch (Exception e) {
            log.error("Error while filling details for {}", requestId, e);
            AlertUtils.showWarning("Failed to load request details.");
        }
    }

    // ==================== SHOW/HIDE ====================
    @Override
    public void onShow() {
        setupListView();
        this.active = true;

        if (!hotkeysRegistered) {
            Scene scene = requestsListView.getScene();
            if (scene != null) {
                registerHotkeys(scene);
                hotkeysRegistered = true;
            }
        }

        filterState.get()
                .ifPresentOrElse(this::applyFilter, this::loadDefaultPage);
        initRealtimeUpdates();
    }

    protected void initRealtimeUpdates() {
        wsClient.connect(
                event -> Platform.runLater(() -> handleEventReceived(List.of(event))),
                event -> Platform.runLater(() -> handleUserEvent(event))
        );
    }

    @Override
    public void onHide() {
        active = false;
        if (uiUpdater != null) {
            uiUpdater.shutdownNow();
            uiUpdater = null;
        }
        activeSearchQuery = null;
        wsClient.disconnect();
        log.info("{} -> onHide() called", getClass().getSimpleName());
    }

    // ==================== PAGINATION ====================
    protected void loadDefaultPage() {
        resetPagination();
        showLoading(true);
        loadNextPage();
    }

    protected void loadNextPage() {
        if (!active || loading || allLoaded) return;
        loading = true;
        showLoading(true);

        int pageToLoad = currentPage;
        RequestFilterDto filterSnapshot = activeFilter;
        String searchSnapshot = activeSearchQuery;

        CompletableFuture.supplyAsync(() -> {
            if (searchSnapshot != null) return requestClient.search(searchSnapshot, pageToLoad, PAGE_SIZE);
            if (filterSnapshot != null) return requestClient.filter(filterSnapshot, pageToLoad, PAGE_SIZE);
            return requestClient.findPage(pageToLoad, PAGE_SIZE, requestType);
        }).thenAccept(result -> Platform.runLater(() -> {
            if (!Objects.equals(activeFilter, filterSnapshot) || !Objects.equals(activeSearchQuery, searchSnapshot)) {
                loading = false;
                showLoading(false);
                return;
            }

            if (result.getContent().isEmpty()) {
                allLoaded = true;

                if (currentPage == 0) {
                    showLoading(false);
                    requestItems.clear();
                    showEmptyMessage(true);
                }
            } else {
                if (currentPage == 0) requestItems.setAll(result.getContent());
                else requestItems.addAll(result.getContent());
                currentPage++;
                showEmptyMessage(false);
            }
            onPageLoaded();
            loading = false;
            showLoading(false);
        })).exceptionally(ex -> {
            log.error("ValidationError loading pageToLoad {}", pageToLoad, ex);
            Platform.runLater(() -> showLoading(false));
            loading = false;
            return null;
        });
    }

    // ==================== FILTER ====================
    public void applyFilter(RequestFilterDto filter) {
        safeUpdate(() -> {
            if (!active) return;
            showEmptyMessage(false);
            showLoading(true);

            activeFilter = filter;
            activeSearchQuery = null;
            filterState.set(filter);
            resetPagination();
            loadNextPage();
        });
    }

    public void clearFilter() {
        safeUpdate(() -> {
            activeFilter = null;
            activeSearchQuery = null;
            filterState.clear();
            resetPagination();
            loadNextPage();
        });
    }

    // ==================== SEARCH ====================
    public void applySearch(String query) {
        safeUpdate(() -> {
            if (!active) return;

            showEmptyMessage(false);
            showLoading(true);

            activeSearchQuery =
                    (query != null && !query.isBlank()) ? query : null;

            activeFilter = null;
            filterState.clear();

            resetPagination();
            loadNextPage();
        });
    }

    protected void resetPagination() {
        currentPage = 0;
        allLoaded = false;
        requestItems.clear();
    }

    // ==================== UI ====================
    private void showLoading(boolean show) {
        if (loadingOverlay == null) return;
        if (show == overlayVisible) return;

        overlayVisible = show;
        loadingIndicator.setVisible(show);

        if (show) {
            loadingOverlay.setOpacity(0);
            loadingOverlay.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), loadingOverlay);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(180), loadingOverlay);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> loadingOverlay.setVisible(false));
            fadeOut.play();
        }
    }

    protected void showEmptyMessage(boolean show) {
        emptyMessageLabel.setVisible(show);
        emptyMessageLabel.setManaged(show);

        requestsListView.setMouseTransparent(show);
        requestsListView.setFocusTraversable(!show);
    }

    // ==================== THREAD SAFETY ====================
    @Override
    public void pauseUpdates() {pauseUpdates = true;}

    @Override
    public void resumeUpdates() {pauseUpdates = false;}

    @Override
    public void safeUpdate(Runnable resetLogic) {
        pauseUpdates();
        try {resetLogic.run();} finally {resumeUpdates();}
    }

    // ==================== WEB SOCKET EVENT HANDLING ====================
    private void handleEventReceived(List<RequestEvent<RequestEventDto>> events) {
        if (!active || pauseUpdates || events.isEmpty()) return;

        boolean relevant = events.stream()
                .map(RequestEvent::getPayload)
                .filter(Objects::nonNull)
                .anyMatch(dto -> requestType == null || dto.type() == requestType);

        if (!relevant) {
            log.debug("WS batch ignored: no events for requestType={}", requestType);
            return;
        }

        List<Long> ids = events.stream().map(RequestEvent::getRequestId).distinct().toList();
        log.debug("WS batch accepted: {} events, affected requestIds={}, requestType={}", events.size(), ids, requestType);

        reloadCurrentPageFromServer();
    }

    private void handleUserEvent(RequestEvent<RequestEventDto> event) {
        log.info("WS USER EVENT received: {}", event);

        if (event == null || event.getPayload() == null) return;
        if (event.getType() != RequestEvent.EventType.UPDATED) return;
        if (loggedInUser == null) return;

        RequestEventDto dto = event.getPayload();
        if (dto.status() != RequestStatusDto.ACCEPTED) return;

        if (!Objects.equals(dto.dispatcherId(), loggedInUser.id())) {
            log.debug("Skip ACCEPTED toast: dispatcherId={}, currentUserId={}", dto.dispatcherId(), loggedInUser.id());
            return;
        }

        ToastService.showInfo(
                windowService.getPrimaryStage(),
                "Request dispatched",
                "Request #" + event.getRequestId() + " has been dispatched to you!"
        );

    }

    private void reloadCurrentPageFromServer() {
        RequestFilterDto filterSnapshot = activeFilter;
        String searchSnapshot = activeSearchQuery;
        int page = 0;

        CompletableFuture.supplyAsync(() -> {
            if (searchSnapshot != null) return requestClient.search(searchSnapshot, page, PAGE_SIZE);
            if (filterSnapshot != null) return requestClient.filter(filterSnapshot, page,  PAGE_SIZE);
            return requestClient.findPage(page, PAGE_SIZE, requestType);
        }).thenAccept(items -> Platform.runLater(() -> {
            requestItems.setAll(items.getContent());
            requestsListView.refresh();
            showEmptyMessage(items.getContent().isEmpty());
        })).exceptionally(ex -> {
            log.error("ValidationError reloading current page from DB", ex);
            Platform.runLater(() -> showEmptyMessage(false));
            return null;
        });
    }

    protected ScrollBar getVerticalScrollBar(ListView<?> listView) {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar && bar.getOrientation() == Orientation.VERTICAL) {
                return bar;
            }
        }
        return null;
    }

    @Override
    public Optional<RequestFilterDto> getLastAppliedFilter() {
        return Optional.ofNullable(activeFilter);
    }

    protected void registerHotkeys(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
            boolean shortcut = (isMac && event.isMetaDown()) || (!isMac && event.isControlDown());

            if (!shortcut) return;

            switch (event.getCode()) {

                case F -> {
                    if (supportsFilterHotkey()) {
                        onFilterHotkey();
                        event.consume();
                    }
                }

                case S -> {
                    if (supportsSearchHotkey()) {
                        onSearchHotkey();
                        event.consume();
                    }
                }
            }
        });
    }

    protected boolean allowDuplicateHotkey() {
        return false;
    }

    protected void onFilterHotkey() {
        if (homeController != null) {
            homeController.handleFilter();
        }
    }
    protected void onPageLoaded() {}
    protected void onSearchHotkey() {}

    protected boolean supportsFilterHotkey() {return false;}
    protected boolean supportsSearchHotkey() {return false;}
}
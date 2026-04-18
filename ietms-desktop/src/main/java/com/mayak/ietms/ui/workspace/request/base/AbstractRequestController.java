package com.mayak.ietms.ui.workspace.request.base;

import com.mayak.ietms.integration.exception.ApiException;
import com.mayak.ietms.request.dto.enums.RequestStatusDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.event.RequestEventDto;
import com.mayak.ietms.infrastructure.toast.ToastService;
import com.mayak.ietms.integration.api.RequestClient;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.request.dto.view.RequestListItemDto;
import com.mayak.ietms.ui.workspace.request.item.RequestItemController;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.home.HomeController;
import com.mayak.ietms.support.state.RequestFilterState;
import com.mayak.ietms.infrastructure.error.AlertUtils;
import com.mayak.ietms.infrastructure.window.WindowService;
import com.mayak.ietms.integration.websocket.RequestStompClient;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    protected final RequestClient requestClient;
    protected final WindowService windowService;
    protected final RequestFilterState filterState;
    protected final RequestStompClient wsClient;

    @Getter @Setter
    HomeController homeController;

    @Getter
    private UserResponseDto loggedInUser;

    @Getter
    protected final ObservableList<RequestListItemDto> requestItems = FXCollections.observableArrayList();
    private final ConcurrentMap<Long, RequestItemController> visible = new ConcurrentHashMap<>();

    protected RequestFilterDto activeFilter;
    protected String activeSearchQuery;

    @Getter @Setter
    protected RequestTypeDto requestType;

    @Getter @Setter
    protected Stage stage;

    private Runnable wsUnsubscribe;

    protected static final int PAGE_SIZE = 100;

    protected int currentPage = 0;
    protected boolean loading = false;
    protected boolean allLoaded = false;

    private boolean overlayVisible = false;
    private boolean hotkeysRegistered = false;

    protected volatile boolean active = true;
    protected volatile boolean pauseUpdates = false;

    protected ScheduledExecutorService uiUpdater;

    private ScheduledExecutorService wsDebouncer;

    private ScheduledFuture<?> pendingReload;

    protected final ConcurrentMap<Long, CompletableFuture<RequestDetailsDto>> detailsCache = new ConcurrentHashMap<>();

    private static final int PREFETCH_RADIUS = 50;


    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
    }

    @Override
    public Optional<RequestFilterDto> getLastAppliedFilter() {
        return Optional.ofNullable(activeFilter);
    }

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

        filterState.get().ifPresentOrElse(this::applyFilter, this::loadDefaultPage);
        wsDebouncer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-debouncer");
            t.setDaemon(true);
            return t;
        });
        initRealtimeUpdates();
    }

    @Override
    public void onHide() {
        active = false;

        if (wsUnsubscribe != null) {
            wsUnsubscribe.run();
            wsUnsubscribe = null;
        }

        if (pendingReload != null) {
            pendingReload.cancel(true);
            pendingReload = null;
        }

        if (wsDebouncer != null) {
            wsDebouncer.shutdownNow();
            wsDebouncer = null;
        }

        if (uiUpdater != null) {
            uiUpdater.shutdownNow();
            uiUpdater = null;
        }
        activeSearchQuery = null;
        log.info("{} -> onHide() called", getClass().getSimpleName());
    }

    @Override
    public void setupListView() {
        requestsListView.setItems(requestItems);
        requestsListView.setCellFactory(lv -> {
            RequestCell cell = new RequestCell(windowService, loggedInUser, this);

            cell.indexProperty().addListener((obs, oldIdx, newIdx) -> {
                int idx = newIdx.intValue();
                if (idx < 0) return;
                prefetchAroundIndex(idx);
            });

            return cell;
        });

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

    protected ScrollBar getVerticalScrollBar(ListView<?> listView) {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar && bar.getOrientation() == Orientation.VERTICAL) {
                return bar;
            }
        }
        return null;
    }

    public CompletableFuture<RequestDetailsDto> getDetailsAsync(Long id) {
        return detailsCache.computeIfAbsent(id,
                key -> CompletableFuture.supplyAsync(() -> requestClient.getDetails(key))
        );
    }

    @Override
    public void invalidateRequest(Long requestId) {
        if (!active || requestId == null) return;
        detailsCache.remove(requestId);
        refreshOne(requestId);
    }

    public void registerVisible(Long id, RequestItemController c) {
        if (id != null && c != null) visible.put(id, c);
    }

    public void unregisterVisible(Long id) {
        if (id == null) return;
        visible.remove(id);
    }

    private void refreshOne(Long id) {
        RequestItemController c = visible.get(id);
        if (c == null) return;

        CompletableFuture
                .supplyAsync(() -> requestClient.getDetails(id))
                .thenAccept(dto -> Platform.runLater(() -> {
                    if (!Objects.equals(c.getRequestId(), id)) return;
                    c.attachDetails(dto);
                }))
                .exceptionally(ex -> {
                    Throwable cause = ex.getCause();

                    if (cause instanceof ApiException apiEx && apiEx.getStatus() != null && apiEx.getStatus().value() == 404) {
                        log.debug("Request {} not found anymore, removing from visible", id);
                        Platform.runLater(() -> visible.remove(id));
                        return null;
                    }

                    log.warn("Failed to refresh request {}", id, ex);
                    return null;
                });
    }

    protected void prefetchAroundIndex(int center) {
        int size = requestItems.size();
        if (size == 0) return;

        int from = Math.max(0, center - PREFETCH_RADIUS);
        int to   = Math.min(size - 1, center + PREFETCH_RADIUS);

        for (int i = from; i <= to; i++) {
            Long id = requestItems.get(i).id();
            getDetailsAsync(id);
        }
    }

    // ==================== Pagination API ====================
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
            if (searchSnapshot != null) return requestClient.search(searchSnapshot, pageToLoad, PAGE_SIZE, requestType);
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

    protected void resetPagination() {
        currentPage = 0;
        allLoaded = false;
        requestItems.clear();
    }

    protected void onPageLoaded() {}


    // ==================== Filter / search API ====================
    public void applyFilter(RequestFilterDto filter) {
        safeUpdate(() -> {
            if (!active) return;

            if (requestType != null
                    && (filter.getRequestTypes() == null || filter.getRequestTypes().isEmpty())) {
                filter.setRequestTypes(List.of(requestType.name()));
            }

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

    protected void loadDetailsAndFillForm(Long requestId) {
        CompletableFuture
                .supplyAsync(() -> requestClient.getDetails(requestId))
                .thenAccept(details ->
                        Platform.runLater(() -> fillFormWithRequest(details))
                )
                .exceptionally(ex -> {
                    log.error("Error while filling details for {}", requestId, ex);
                    Platform.runLater(() ->
                            AlertUtils.showWarning("Failed to load request details.")
                    );
                    return null;
                });
    }

    // ==================== UI ====================
    protected Stage getOwnerStage() {
        Stage s = getStage();
        if (s != null && s.isShowing()) return s;
        return windowService.getPrimaryStage();
    }

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
    protected void initRealtimeUpdates() {
        wsUnsubscribe = wsClient.connect(event -> handleEventReceived(List.of(event)), this::handleUserEvent);
    }

    private void handleEventReceived(List<RequestEvent<RequestEventDto>> events) {
        if (!active || pauseUpdates || events.isEmpty()) return;

        boolean needsReload = events.stream()
                .map(RequestEvent::getPayload)
                .filter(Objects::nonNull)
                .map(RequestEventDto::status)
                .anyMatch(Objects::nonNull);

        boolean hasDeleted = events.stream()
                .anyMatch(e -> e.getType() == RequestEvent.EventType.DELETED);

        if (needsReload || hasDeleted) scheduleSortedReload();

        events.stream().filter(e -> e.getType() != RequestEvent.EventType.DELETED)
                .map(RequestEvent::getRequestId)
                .distinct()
                .forEach(this::invalidateRequest);
    }

    private void scheduleSortedReload() {
        if (wsDebouncer == null || wsDebouncer.isShutdown()) return;
        if (pendingReload != null && !pendingReload.isDone()) {
            pendingReload.cancel(false);
        }
        pendingReload = wsDebouncer.schedule(this::reloadSortedPageFromServer, 120, TimeUnit.MILLISECONDS);
    }

    private void reloadSortedPageFromServer() {
        RequestFilterDto filterSnapshot = activeFilter;
        String searchSnapshot = activeSearchQuery;

        CompletableFuture
                .supplyAsync(() -> {
                    if (searchSnapshot != null)
                        return requestClient.search(searchSnapshot, 0, PAGE_SIZE, requestType);
                    if (filterSnapshot != null)
                        return requestClient.filter(filterSnapshot, 0, PAGE_SIZE);
                    return requestClient.findPage(0, PAGE_SIZE, requestType);
                })
                .thenAccept(result -> Platform.runLater(() -> {
                    requestItems.setAll(result.getContent());
                    currentPage = 1;
                    allLoaded = result.getContent().size() < PAGE_SIZE;
                    showEmptyMessage(result.getContent().isEmpty());
                }));
    }

    private void handleUserEvent(RequestEvent<RequestEventDto> event) {
        if (event == null || event.getPayload() == null) return;
        if (event.getType() != RequestEvent.EventType.UPDATED) return;

        RequestEventDto dto = event.getPayload();
        if (dto.status() != RequestStatusDto.ACCEPTED) return;

        ToastService.showInfo(
                getOwnerStage(),
                "Request dispatched",
                "Request #" + event.getRequestId() + " has been dispatched to you!"
        );

    }

    // ==================== Hotkeys ====================
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
            homeController.handleFilter(getStage());
        }
    }

    protected void onSearchHotkey() {}

    protected boolean supportsFilterHotkey() {return false;}
    protected boolean supportsSearchHotkey() {return false;}
}
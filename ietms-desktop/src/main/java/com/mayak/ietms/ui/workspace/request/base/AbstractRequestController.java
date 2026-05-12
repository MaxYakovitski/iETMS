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
import net.rgielen.fxweaver.core.FxWeaver;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * Base controller for request list views (client and transport).
 *
 * <p>Provides paginated loading, infinite scroll, details caching,
 * filter/search coordination, real-time WebSocket updates with debounced
 * sorted reload, and global hotkey registration.
 *
 * <p>Subclasses must be annotated with {@link net.rgielen.fxweaver.core.FxmlView}
 * and declare {@link com.mayak.ietms.ui.core.RequiresPermission} where applicable.
 * The {@link #requestType} field must be set before {@link #onShow()} is called.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRequestController implements ViewLifecycle, SecuredView, RequestsParent {

    // ==================== Constants ====================
    private static final int PAGE_SIZE = 100;
    private static final int PREFETCH_RADIUS = 50;

    // ==================== FXML ====================
    @FXML
    private ListView<RequestListItemDto> requestsListView;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private Label emptyMessageLabel;

    // ==================== Dependencies ====================
    protected final RequestClient requestClient;
    protected final WindowService windowService;
    private final FxWeaver fxWeaver;
    protected final RequestFilterState filterState;
    private final RequestStompClient wsClient;

    // ==================== State ====================
    @Getter @Setter
    private HomeController homeController;

    @Getter
    private UserResponseDto loggedInUser;

    @Getter @Setter
    protected RequestTypeDto requestType;

    @Getter @Setter
    protected Stage stage;

    private final ObservableList<RequestListItemDto> requestItems = FXCollections.observableArrayList();
    private final ConcurrentMap<Long, RequestItemController> visible = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, CompletableFuture<RequestDetailsDto>> detailsCache = new ConcurrentHashMap<>();

    private RequestFilterDto activeFilter;
    private String activeSearchQuery;

    // ==================== Pagination ====================
    private int currentPage = 0;
    private boolean loading = false;
    private boolean allLoaded = false;

    // ==================== Concurrency ====================
    private volatile boolean active = true;
    private volatile boolean pauseUpdates = false;

    private boolean overlayVisible = false;

    /** Deferred to onShow() — scene is not yet attached during initialize(). */
    private boolean hotkeysRegistered = false;

    private ScheduledExecutorService uiUpdater;
    private ScheduledExecutorService wsDebouncer;
    private ScheduledFuture<?> pendingReload;
    private Runnable wsUnsubscribe;

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
    }

    @Override
    public Optional<RequestFilterDto> getLastAppliedFilter() {
        return Optional.ofNullable(activeFilter);
    }

    // ==================== Lifecycle ====================
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

    /**
     * Wires the list view: sets the cell factory, attaches the infinite-scroll
     * listener (deferred until the skin is applied), and registers the Ctrl+D hotkey
     * on the list itself for controllers that support {@link #allowDuplicateHotkey()}.
     */
    @Override
    public void setupListView() {
        requestsListView.setItems(requestItems);
        requestsListView.setCellFactory(lv -> {
            RequestCell cell = new RequestCell(fxWeaver, loggedInUser, this);

            cell.indexProperty().addListener((obs, oldIdx, newIdx) -> {
                int idx = newIdx.intValue();
                if (idx < 0) return;
                prefetchAroundIndex(idx);
            });

            return cell;
        });

        // Scroll bar does not exist until the skin is applied — attach the
        // pagination listener only after skinProperty fires.
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

    private ScrollBar getVerticalScrollBar(ListView<?> listView) {
        for (Node node : listView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar && bar.getOrientation() == Orientation.VERTICAL) {
                return bar;
            }
        }
        return null;
    }

    // ==================== Pagination ====================
    private void loadDefaultPage() {
        resetPagination();
        showLoading(true);
        loadNextPage();
    }

    protected void loadNextPage() {
        if (!active || loading || allLoaded) return;
        loading = true;
        showLoading(true);

        // Snapshot filter/search before the async call. After the result
        // arrives we compare against the current values to discard responses
        // that belong to a superseded query.
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
            log.error("Failed to load page {}", pageToLoad, ex);
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

    // ==================== Filter / Search ====================
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

    // ==================== Cache ====================

    /**
     * Returns cached details for the given request, fetching from the backend
     * if not already cached. Concurrent callers for the same {@code id} share
     * the same {@link java.util.concurrent.CompletableFuture}.
     */
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

    /** Registers a visible cell controller so it can receive inline refresh calls. */
    public void registerVisible(Long id, RequestItemController c) {
        if (id != null && c != null) visible.put(id, c);
    }

    /** Removes the cell controller from the visible registry. */
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

    /**
     * Eagerly fetches details for items within PREFETCH_RADIUS of the visible
     * center so that opening a row feels instantaneous.
     */
    private void prefetchAroundIndex(int center) {
        int size = requestItems.size();
        if (size == 0) return;

        int from = Math.max(0, center - PREFETCH_RADIUS);
        int to   = Math.min(size - 1, center + PREFETCH_RADIUS);

        for (int i = from; i <= to; i++) {
            Long id = requestItems.get(i).id();
            getDetailsAsync(id);
        }
    }

    // ==================== Form ====================
    private void loadDetailsAndFillForm(Long requestId) {
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
    private Stage getOwnerStage() {
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

    /**
     * Wraps filter/search resets in a pause-resume bracket so that any
     * in-flight WebSocket events do not interleave with the reset logic.
     */
    @Override
    public void safeUpdate(Runnable resetLogic) {
        pauseUpdates();
        try {resetLogic.run();} finally {resumeUpdates();}
    }

        // ==================== WebSocket ====================
    private void initRealtimeUpdates() {
        wsUnsubscribe = wsClient.connect(event -> handleEventReceived(List.of(event)), this::handleUserEvent);
    }

    /**
     * A sorted reload is triggered when any event carries a status change,
     * since a status change may affect the item's position in the sorted list.
     */
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

    // debounce: cancel the previous pending reload and reschedule 120 ms later
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
    private void registerHotkeys(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
            boolean shortcut = (isMac && event.isMetaDown()) || (!isMac && event.isControlDown());

            if (!shortcut) return;

            if (event.getCode() == KeyCode.F && supportsFilterHotkey()) {
                        onFilterHotkey();
                        event.consume();
            }
        });
    }

    /**
     * When true, the list also handles Ctrl+D to fill the form from the
     * selected item. Subclasses that own a form panel should override this.
     */
    protected boolean allowDuplicateHotkey() {
        return false;
    }

    protected void onFilterHotkey() {
        if (homeController != null) {
            homeController.handleFilter(getStage());
        }
    }

    protected boolean supportsFilterHotkey() {
        return false;
    }
}
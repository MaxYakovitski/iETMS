package com.mayak.ietms.ui.core;

/**
 * Lifecycle contract for JavaFX controllers managed by {@link com.mayak.ietms.infrastructure.window.WindowService}
 * and {@link com.mayak.ietms.ui.home.HomeController}.
 *
 * <p>All methods have default no-op implementations so controllers
 * can selectively override only what they need.
 */
public interface ViewLifecycle {

    /** Called once after the view's FXML node has been added to the scene and is about to become visible. */
    default void setupListView() {}

    /** Called each time the view is shown (e.g. window opened, tab switched to). */
    default void onShow() {}

    /** Called each time the view is hidden (e.g. window closed, navigated away). */
    default void onHide() {}
}
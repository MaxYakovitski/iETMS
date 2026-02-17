package com.mayak.ietms.ui.core;

public interface ViewLifecycle {
    default void setupListView() {}
    default void onShow() {}
    default void onHide() {}
}